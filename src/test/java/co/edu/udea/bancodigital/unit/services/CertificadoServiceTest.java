package co.edu.udea.bancodigital.unit.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import co.edu.udea.bancodigital.dtos.responses.CertificadoBancarioDTO;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.models.pks.UsuarioId;
import co.edu.udea.bancodigital.services.CertificadoService;
import co.edu.udea.bancodigital.services.FinancialReportCommonService;
import co.edu.udea.bancodigital.services.MailService;
import co.edu.udea.bancodigital.services.ReporteGeneratorService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CertificadoServiceTest - Pruebas de Generación de Certificados Bancarios")
class CertificadoServiceTest {

    @Mock
    private ReporteGeneratorService reporteGeneratorService;

    @Mock
    private MailService mailService;

    @Mock
    private FinancialReportCommonService commonService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CertificadoService certificadoService;

    private UUID cuentaId;
    private Usuario usuario;
    private Cuenta cuenta;
    private TipoCuenta tipoCuenta;
    private EstadoCuenta estadoCuenta;

    @BeforeEach
    void setUp() {
        // Arrange
        cuentaId = UUID.randomUUID();
        
        usuario = Usuario.builder()
                .id(new UsuarioId(1, "123"))
                .correo("usuario@example.com")
                .nombre("Juan")
                .primerApellido("Pérez")
                .segundoApellido("García")
                .build();

        tipoCuenta = new TipoCuenta();
        tipoCuenta.setId(1);
        tipoCuenta.setNombre("Cuenta de Ahorros Digital");

        estadoCuenta = new EstadoCuenta();
        estadoCuenta.setId(1);
        estadoCuenta.setNombre("ACTIVA");

        cuenta = Cuenta.builder()
                .idCuenta(cuentaId)
                .dueno(usuario)
                .saldo(BigDecimal.valueOf(500000))
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoCuenta)
                .build();
        cuenta.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));

        configureSecurityContext("usuario@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void configureSecurityContext(String email) {
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    // ==================== HISTORIA 2: REPORTE DE CUENTAS (CERTIFICADO) ====================

    @Test
    @DisplayName("HU2-SC1: Generar reporte de certificado bancario exitosamente")
    void should_GenerateBankCertificate_When_AllConditionsAreMet() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez García");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDF(any())).thenReturn(new byte[]{1, 2, 3});
        when(reporteGeneratorService.generarCSV(any())).thenReturn(new byte[]{4, 5, 6});

        // Act
        assertDoesNotThrow(() -> certificadoService.solicitarReporteCertificadoBancario(cuentaId));

        // Assert
        verify(commonService).obtenerUsuarioAutenticado();
        verify(commonService).obtenerCuentaConValidacion(cuentaId);
        verify(commonService).validarPropietarioCuenta(cuenta, usuario);
        verify(reporteGeneratorService).generarPDF(any());
        verify(reporteGeneratorService).generarCSV(any());
        verify(mailService).sendEmailWithAttachments(eq(usuario.getCorreo()), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU2-SC1: Validar que certificado contiene nombre completo, fecha de consulta y datos de cuenta")
    void should_IncludeAllRequiredDataInCertificate() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez García");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDF(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSV(any())).thenReturn(new byte[]{2});

        // Act
        certificadoService.solicitarReporteCertificadoBancario(cuentaId);

        // Assert
        ArgumentCaptor<CertificadoBancarioDTO> captor = ArgumentCaptor.forClass(CertificadoBancarioDTO.class);
        verify(reporteGeneratorService, times(1)).generarPDF(captor.capture());

        CertificadoBancarioDTO certificadoCapturado = captor.getValue();
        assertEquals("Juan Pérez García", certificadoCapturado.getNombreCompleto());
        assertEquals("Cuenta de Ahorros Digital", certificadoCapturado.getNombreProducto());
        assertEquals("12345678", certificadoCapturado.getNumeroCuenta());
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0), certificadoCapturado.getFechaInicio());
        assertEquals(BigDecimal.valueOf(500000), certificadoCapturado.getBalance());
        assertEquals("ACTIVA", certificadoCapturado.getEstadoCuenta());
        assertNotNull(certificadoCapturado.getFechaConsulta());
    }

    @Test
    @DisplayName("HU2-SC1: Generar certificado con saldo actual y estado de cuenta")
    void should_IncludeCurrentBalanceAndAccountStatus() {
        // Arrange
        Cuenta cuentaConSaldo = Cuenta.builder()
                .idCuenta(cuentaId)
                .dueno(usuario)
                .saldo(BigDecimal.valueOf(1500000))
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoCuenta)
                .build();
        cuentaConSaldo.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));

        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuentaConSaldo);
        doNothing().when(commonService).validarPropietarioCuenta(cuentaConSaldo, usuario);
        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez García");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDF(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSV(any())).thenReturn(new byte[]{2});

        // Act
        certificadoService.solicitarReporteCertificadoBancario(cuentaId);

        // Assert
        ArgumentCaptor<CertificadoBancarioDTO> captor = ArgumentCaptor.forClass(CertificadoBancarioDTO.class);
        verify(reporteGeneratorService).generarPDF(captor.capture());

        CertificadoBancarioDTO certificado = captor.getValue();
        assertEquals(BigDecimal.valueOf(1500000), certificado.getBalance());
        assertEquals("ACTIVA", certificado.getEstadoCuenta());
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar certificado cuando usuario no está autenticado")
    void should_RejectCertificate_When_UserIsNotAuthenticated() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado())
                .thenThrow(new EntityNotFoundException("Usuario autenticado no encontrado"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> certificadoService.solicitarReporteCertificadoBancario(cuentaId));

        verify(commonService).obtenerUsuarioAutenticado();
        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar certificado si cuenta no existe")
    void should_RejectCertificate_When_AccountDoesNotExist() {
        // Arrange
        UUID invalidCuentaId = UUID.randomUUID();
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(invalidCuentaId))
                .thenThrow(new EntityNotFoundException("Cuenta con id " + invalidCuentaId + " no existe"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> certificadoService.solicitarReporteCertificadoBancario(invalidCuentaId));

        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar certificado si usuario no es propietario de la cuenta")
    void should_RejectCertificate_When_UserIsNotAccountOwner() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doThrow(new AccessDeniedException("No tienes permisos para acceder a esta cuenta"))
                .when(commonService).validarPropietarioCuenta(cuenta, usuario);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> certificadoService.solicitarReporteCertificadoBancario(cuentaId));

        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU2: Enviar certificado por correo exitosamente")
    void should_SendCertificateByEmail_When_ReportIsGenerated() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez García");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDF(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSV(any())).thenReturn(new byte[]{2});

        // Act
        certificadoService.solicitarReporteCertificadoBancario(cuentaId);

        // Assert
        verify(mailService, times(1)).sendEmailWithAttachments(
                eq("usuario@example.com"),
                anyString(),
                anyString(),
                any());
    }

    @Test
    @DisplayName("HU2: Validar que ambos formatos (PDF y CSV) se generan")
    void should_GenerateBothPDFAndCSVFormats() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez García");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDF(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSV(any())).thenReturn(new byte[]{2});

        // Act
        certificadoService.solicitarReporteCertificadoBancario(cuentaId);

        // Assert
        verify(reporteGeneratorService, times(1)).generarPDF(any());
        verify(reporteGeneratorService, times(1)).generarCSV(any());
    }

}
