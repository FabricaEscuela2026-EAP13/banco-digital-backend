package co.edu.udea.bancodigital.services;

import co.edu.udea.bancodigital.dtos.responses.CertificadoBancarioDTO;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CertificadoService {

    private final ReporteGeneratorService reporteGeneratorService;
    private final MailService mailService;
    private final FinancialReportCommonService commonService;

    public void solicitarReporteCertificadoBancario(UUID idCuenta) {
        Usuario usuarioAutenticado = commonService.obtenerUsuarioAutenticado();
        log.info("Usuario {} solicita reporte certificado bancario para cuenta {}",
                usuarioAutenticado.getCorreo(), idCuenta);

        Cuenta cuenta = commonService.obtenerCuentaConValidacion(idCuenta);
        commonService.validarPropietarioCuenta(cuenta, usuarioAutenticado);

        CertificadoBancarioDTO certificado = mapearCertificadoDTO(cuenta);

        byte[] pdf = reporteGeneratorService.generarPDF(certificado);
        byte[] csv = reporteGeneratorService.generarCSV(certificado);

        enviarCertificado(usuarioAutenticado, certificado, pdf, csv);
    }

    private void enviarCertificado(Usuario usuario, CertificadoBancarioDTO certificado,
                                   byte[] pdf, byte[] csv) {
        String asunto = "Tu Reporte del Certificado Bancario - " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String cuerpo = construirCuerpoCertificado(certificado);

        Map<String, byte[]> adjuntos = new HashMap<>();
        String nombreArchivo = generarNombreArchivo(certificado);
        adjuntos.put(nombreArchivo + ".pdf", pdf);
        adjuntos.put(nombreArchivo + ".csv", csv);

        try {
            mailService.sendEmailWithAttachments(usuario.getCorreo(), asunto, cuerpo, adjuntos);
            log.info("Reporte certificado bancario enviado exitosamente a: {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("Error enviando certificado por email a {}: {}", usuario.getCorreo(), e.getMessage(), e);
            throw new IllegalArgumentException("Error enviando certificado por email", e);
        }
    }

    private CertificadoBancarioDTO mapearCertificadoDTO(Cuenta cuenta) {
        Usuario usuario = cuenta.getDueno();
        String nombreCompleto = commonService.construirNombreCompleto(usuario);

        return CertificadoBancarioDTO.builder()
                .nombreCompleto(nombreCompleto)
                .fechaConsulta(LocalDate.now())
                .nombreProducto(cuenta.getTipoCuenta().getNombre())
                .numeroCuenta(commonService.formatearNumeroCuenta(cuenta.getIdCuenta()))
                .fechaInicio(cuenta.getCreatedAt())
                .balance(cuenta.getSaldo())
                .estadoCuenta(cuenta.getEstadoCuenta().getNombre())
                .build();
    }

    private String construirCuerpoCertificado(CertificadoBancarioDTO certificado) {
        return String.format("""
                <html>
                    <body>
                        <h2>Tu Certificado Bancario</h2>
                        <p>Hola %s,</p>
                        <p>Adjuntamos tu certificado bancario solicitado en formato PDF y CSV.</p>
                        <p><strong>Detalles del Certificado:</strong></p>
                        <ul>
                            <li>Producto: %s</li>
                            <li>Número de Cuenta: %s</li>
                            <li>Balance: $%,.2f</li>
                            <li>Estado: %s</li>
                            <li>Fecha de Consulta: %s</li>
                        </ul>
                        <p>Puedes descargar los archivos adjuntos para tu registro.</p>
                        <br>
                        <p>Saludos,<br>Equipo Banco Digital</p>
                    </body>
                </html>
                """,
                certificado.getNombreCompleto(),
                certificado.getNombreProducto(),
                certificado.getNumeroCuenta(),
                certificado.getBalance(),
                certificado.getEstadoCuenta(),
                certificado.getFechaConsulta().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private String generarNombreArchivo(CertificadoBancarioDTO certificado) {
        String nombre = certificado.getNombreCompleto().replace(" ", "");
        String ultimos8Digitos = certificado.getNumeroCuenta().substring(certificado.getNumeroCuenta().length() - 8);
        return "CertificadoCuenta_" + nombre + "_" + ultimos8Digitos;
    }
}
