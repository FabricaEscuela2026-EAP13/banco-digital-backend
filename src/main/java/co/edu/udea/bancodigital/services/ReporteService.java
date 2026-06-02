package co.edu.udea.bancodigital.services;

import co.edu.udea.bancodigital.dtos.responses.ReporteMovimientosDTO;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Transaccion;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.repositories.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final TransaccionRepository transaccionRepository;
    private final ReporteGeneratorService reporteGeneratorService;
    private final MailService mailService;
    private final FinancialReportCommonService commonService;

    @Transactional(readOnly = true)
    public void generarReporteMovimientos(UUID idCuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        Usuario usuarioAutenticado = commonService.obtenerUsuarioAutenticado();
        log.info("Usuario {} solicita reporte de movimientos para cuenta {} de {} a {}",
                usuarioAutenticado.getCorreo(), idCuenta, fechaInicio, fechaFin);

        Cuenta cuenta = commonService.obtenerCuentaConValidacion(idCuenta);
        commonService.validarPropietarioCuenta(cuenta, usuarioAutenticado);

        ReporteMovimientosDTO reporte = generarDTOReporte(idCuenta, cuenta, usuarioAutenticado, fechaInicio, fechaFin);

        byte[] pdf = reporteGeneratorService.generarPDFReporte(reporte);
        byte[] csv = reporteGeneratorService.generarCSVReporte(reporte);

        enviarReporteMovimientos(usuarioAutenticado, reporte, pdf, csv, fechaInicio, fechaFin);
    }

    private ReporteMovimientosDTO generarDTOReporte(UUID idCuenta, Cuenta cuenta, Usuario usuario,
                                                     LocalDate fechaInicio, LocalDate fechaFin) {
        Pageable pageable = PageRequest.of(0, 100);
        Page<Transaccion> transacciones = transaccionRepository.findHistorialByCuentaAndFechaHoraBetween(
                idCuenta,
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(LocalTime.MAX),
                pageable);

        return ReporteMovimientosDTO.builder()
                .nombreCliente(commonService.construirNombreCompleto(usuario))
                .numeroCuenta(commonService.formatearNumeroCuenta(cuenta.getIdCuenta()))
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .fechaConsulta(LocalDate.now())
                .saldoFinal(cuenta.getSaldo())
                .transacciones(transacciones.getContent())
                .build();
    }

    private void enviarReporteMovimientos(Usuario usuario, ReporteMovimientosDTO reporte,
                                          byte[] pdf, byte[] csv,
                                          LocalDate fechaInicio, LocalDate fechaFin) {
        String asunto = "Tu Reporte de Movimientos - " + fechaInicio + " a " + fechaFin;
        String cuerpo = construirCuerpoReporte(reporte);

        Map<String, byte[]> adjuntos = new HashMap<>();
        String nombreArchivo = generarNombreArchivo(reporte);
        adjuntos.put(nombreArchivo + ".pdf", pdf);
        adjuntos.put(nombreArchivo + ".csv", csv);

        try {
            mailService.sendEmailWithAttachments(usuario.getCorreo(), asunto, cuerpo, adjuntos);
            log.info("Reporte de movimientos enviado exitosamente a: {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("Error enviando reporte por email a {}: {}", usuario.getCorreo(), e.getMessage(), e);
            throw new IllegalArgumentException("Error enviando reporte por email", e);
        }
    }

    private String generarNombreArchivo(ReporteMovimientosDTO reporte) {
        String ultimos4Digitos = reporte.getNumeroCuenta().substring(reporte.getNumeroCuenta().length() - 4);
        return "ReporteMovimientos_" + reporte.getNombreCliente().replace(" ", "") + "_" + ultimos4Digitos;
    }

    private String construirCuerpoReporte(ReporteMovimientosDTO reporte) {
        return String.format("""
                <html>
                    <body>
                        <h2>Tu Reporte de Movimientos</h2>
                        <p>Hola %s,</p>
                        <p>Adjuntamos tu reporte de movimientos solicitado en formato PDF y CSV.</p>
                        <p><strong>Detalles del Reporte:</strong></p>
                        <ul>
                            <li>Período: %s a %s</li>
                            <li>Número de Cuenta: %s</li>
                            <li>Saldo Final: $%,.2f</li>
                            <li>Total Movimientos: %d</li>
                        </ul>
                        <p>Puedes descargar los archivos adjuntos para tu registro.</p>
                        <br>
                        <p>Saludos,<br>Equipo Banco Digital</p>
                    </body>
                </html>
                """,
                reporte.getNombreCliente(),
                reporte.getFechaInicio(),
                reporte.getFechaFin(),
                reporte.getNumeroCuenta(),
                reporte.getSaldoFinal(),
                reporte.getTransacciones() != null ? reporte.getTransacciones().size() : 0);
    }
}
