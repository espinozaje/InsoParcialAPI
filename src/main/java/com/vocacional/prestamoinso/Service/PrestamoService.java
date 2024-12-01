package com.vocacional.prestamoinso.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.DTO.SunatResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.CronogramaPagos;
import com.vocacional.prestamoinso.Entity.Prestamo;
import com.vocacional.prestamoinso.Mapper.ClienteMapper;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Repository.CronogramaPagosRepository;
import com.vocacional.prestamoinso.Repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrestamoService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CronogramaPagosRepository cronogramaPagosRepository;

    @Autowired
    private ClienteService clienteService;


    public List<Prestamo> listarPrestamosPendientes() {
        // Obtener todos los préstamos
        List<Prestamo> prestamos = prestamoRepository.findAll();
        List<Prestamo> prestamosConPagosPendientes = new ArrayList<>();

        for (Prestamo prestamo : prestamos) {
            // Filtrar los pagos pendientes del préstamo
            List<CronogramaPagos> pagosPendientes = cronogramaPagosRepository.findByPrestamoIdAndEstadoOrderByFechaPagoAsc(prestamo.getId(), "Pendiente");


            if (!pagosPendientes.isEmpty()) {
                prestamo.setCronogramaPagos(pagosPendientes);
                prestamosConPagosPendientes.add(prestamo);
            }
        }

        return prestamosConPagosPendientes;
    }


    public void eliminarPrestamo(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
        prestamoRepository.delete(prestamo);
    }


    public Prestamo crearPrestamo(String clienteId, double monto, int plazo, double interes) {
        Cliente cliente = clienteRepository.findByNroDocumento(clienteId);

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setNroDocumento(cliente.getNroDocumento());
        prestamo.setInteres(interes);
        prestamo.setMonto(monto);
        prestamo.setPlazo(plazo);

        // Generar cronograma de pagos
        List<CronogramaPagos> cronograma = generarCronograma(prestamo);
        for (CronogramaPagos pago : cronograma) {
            pago.setPrestamo(prestamo);
        }
        prestamo.setCronogramaPagos(cronograma);

        // Guardar el préstamo
        Prestamo prestamoGuardado = prestamoRepository.save(prestamo);

        // Obtener los datos del cliente (DNI o RUC)
        if (isDni(cliente.getNroDocumento())) {
            ReniecResponseDTO reniecResponse = clienteService.validarDNI(cliente.getNroDocumento());
            try {
                generarPdfPrestamo(prestamoGuardado, cronograma, reniecResponse);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al generar el PDF del préstamo");
            }
        } else {
            SunatResponseDTO sunatResponse = clienteService.validarRUC(cliente.getNroDocumento());
            try {
                generarPdfPrestamo(prestamoGuardado, cronograma, sunatResponse);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al generar el PDF del préstamo");
            }
        }

        return prestamoGuardado;
    }

    private boolean isDni(String nroDocumento) {
        // La lógica para determinar si es un DNI o un RUC podría basarse en la longitud o el formato del número
        return nroDocumento.length() == 8;  // El DNI tiene 8 dígitos en Perú
    }


    private void generarPdfPrestamo(Prestamo prestamo, List<CronogramaPagos> cronograma, ReniecResponseDTO reniecResponse) throws IOException {
        String pdfPath = "prestamo_" + prestamo.getId() + ".pdf";
        PdfWriter writer = new PdfWriter(pdfPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        document.add(new Paragraph("Detalle del Préstamo")
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        // Detalles del préstamo (DNI)
        document.add(new Paragraph("Cliente: " + reniecResponse.getNombres() + " " + reniecResponse.getApellidoPaterno() + " " + reniecResponse.getApellidoMaterno()));
        document.add(new Paragraph("Numero de Documento: " + reniecResponse.getNumeroDocumento()));
        document.add(new Paragraph("Monto: " + prestamo.getMonto()));
        document.add(new Paragraph("Interés Mensual: " + prestamo.getInteres() + "%"));
        document.add(new Paragraph("Plazo: " + prestamo.getPlazo() + " meses"));
        document.add(new Paragraph("Fecha de Creación: " + LocalDate.now()));
        document.add(new Paragraph("\n"));

        // Tabla de cronograma de pagos
        Table table = new Table(new float[]{1, 3, 3, 2, 3, 3, 3});
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell("N°");
        table.addHeaderCell("Fecha de Pago");
        table.addHeaderCell("Monto Cuota");
        table.addHeaderCell("Pago Intereses");
        table.addHeaderCell("Amortización");
        table.addHeaderCell("Saldo Restante");
        table.addHeaderCell("Estado");

        int index = 1;
        for (CronogramaPagos pago : cronograma) {
            table.addCell(String.valueOf(index++));
            table.addCell(pago.getFechaPago().toString());
            table.addCell(String.format("%.2f", pago.getMontoCuota()));
            table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Mostrar el pago de intereses
            table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Mostrar la amortización
            table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Mostrar el saldo restante
            table.addCell(pago.getEstado());
        }

        document.add(table);

        // Cierre del documento
        document.close();
        System.out.println("PDF generado en: " + pdfPath);
    }


    private void generarPdfPrestamo(Prestamo prestamo, List<CronogramaPagos> cronograma, SunatResponseDTO sunatResponse) throws IOException {
        String pdfPath = "prestamo_" + prestamo.getId() + ".pdf";
        PdfWriter writer = new PdfWriter(pdfPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Título
        document.add(new Paragraph("Detalle del Préstamo")
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        // Detalles del préstamo (RUC)
        System.out.println(prestamo.getInteres());
        document.add(new Paragraph("Razon Social: " + sunatResponse.getRazonSocial()));
        document.add(new Paragraph("Tipo de Documento: " + sunatResponse.getTipoDocumento()));
        document.add(new Paragraph("Numero de Documento: " + sunatResponse.getNumeroDocumento()));
        document.add(new Paragraph("Dirección: " + sunatResponse.getDireccion()));
        document.add(new Paragraph("Distrito: " + sunatResponse.getDistrito()));
        document.add(new Paragraph("Provincia: " + sunatResponse.getProvincia()));
        document.add(new Paragraph("Departamento: " + sunatResponse.getDepartamento()));
        document.add(new Paragraph("Monto: " + prestamo.getMonto()));
        document.add(new Paragraph("Interés Mensual: " + prestamo.getInteres() + "%"));
        document.add(new Paragraph("Plazo: " + prestamo.getPlazo() + " meses"));
        document.add(new Paragraph("Fecha de Creación: " + LocalDate.now()));
        document.add(new Paragraph("\n"));

        // Tabla de cronograma de pagos
        Table table = new Table(new float[]{1, 3, 3, 2, 3, 3, 3});
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell("N°");
        table.addHeaderCell("Fecha de Pago");
        table.addHeaderCell("Monto Cuota");
        table.addHeaderCell("Pago Intereses");
        table.addHeaderCell("Amortización");
        table.addHeaderCell("Saldo Restante");
        table.addHeaderCell("Estado");

        int index = 1;
        for (CronogramaPagos pago : cronograma) {
            table.addCell(String.valueOf(index++));
            table.addCell(pago.getFechaPago().toString());
            table.addCell(String.format("%.2f", pago.getMontoCuota()));
            table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Mostrar el pago de intereses
            table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Mostrar la amortización
            table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Mostrar el saldo restante
            table.addCell(pago.getEstado());
        }

        document.add(table);

        // Cierre del documento
        document.close();
        System.out.println("PDF generado en: " + pdfPath);
    }


    public byte[] generarPdf(Long prestamoId) throws IOException {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
        List<CronogramaPagos> cronograma = cronogramaPagosRepository.findByPrestamoId(prestamoId);


        if (prestamo.getNroDocumento().length() == 8) {
            // Crear el PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            document.add(new Paragraph("Detalle del Préstamo")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            // Detalles del préstamo
            document.add(new Paragraph("Cliente: " + prestamo.getCliente().getNombre() + " " + prestamo.getCliente().getApellidoPaterno() + " " + prestamo.getCliente().getApellidoMaterno()));
            document.add(new Paragraph("Nro de Documento: " + prestamo.getNroDocumento()));
            document.add(new Paragraph("Monto: " + prestamo.getMonto()));
            document.add(new Paragraph("Interés: " + prestamo.getInteres() + "%"));
            document.add(new Paragraph("Plazo: " + prestamo.getPlazo() + " meses"));
            document.add(new Paragraph("Fecha de Creación: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            // Tabla de cronograma de pagos
            Table table = new Table(new float[]{1, 3, 3, 2, 3, 3, 3});
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("N°");
            table.addHeaderCell("Fecha de Pago");
            table.addHeaderCell("Monto Cuota");
            table.addHeaderCell("Pago Intereses");
            table.addHeaderCell("Amortización");
            table.addHeaderCell("Saldo Restante");
            table.addHeaderCell("Estado");

            int index = 1;
            for (CronogramaPagos pago : cronograma) {
                table.addCell(String.valueOf(index++));
                table.addCell(pago.getFechaPago().toString());
                table.addCell(String.format("%.2f", pago.getMontoCuota()));
                table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Mostrar el pago de intereses
                table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Mostrar la amortización
                table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Mostrar el saldo restante
                table.addCell(pago.getEstado());
            }

            document.add(table);

            // Cierre del documento
            document.close();

            // Retornar el PDF como un byte array
            return baos.toByteArray();
        } else {

            // Crear el PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            document.add(new Paragraph("Detalle del Préstamo")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            // Detalles del préstamo
            document.add(new Paragraph("Cliente: " + prestamo.getCliente().getNombre() ));
            document.add(new Paragraph("Nro de Documento: " + prestamo.getNroDocumento()));
            document.add(new Paragraph("Direccion: " + prestamo.getCliente().getDireccion()));
            document.add(new Paragraph("Distrito: " + prestamo.getCliente().getDistrito()));
            document.add(new Paragraph("Departamento: " + prestamo.getCliente().getDepartamento()));
            document.add(new Paragraph("Provincia: " + prestamo.getCliente().getProvincia()));
            document.add(new Paragraph("Monto: " + prestamo.getMonto()));
            document.add(new Paragraph("Interés: " + prestamo.getInteres() + "%"));
            document.add(new Paragraph("Plazo: " + prestamo.getPlazo() + " meses"));
            document.add(new Paragraph("Fecha de Creación: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            // Tabla de cronograma de pagos
            Table table = new Table(new float[]{1, 3, 3, 2, 3, 3, 3});
            table.setWidth(UnitValue.createPercentValue(100));
            table.addHeaderCell("N°");
            table.addHeaderCell("Fecha de Pago");
            table.addHeaderCell("Monto Cuota");
            table.addHeaderCell("Pago Intereses");
            table.addHeaderCell("Amortización");
            table.addHeaderCell("Saldo Restante");
            table.addHeaderCell("Estado");

            int index = 1;
            for (CronogramaPagos pago : cronograma) {
                table.addCell(String.valueOf(index++));
                table.addCell(pago.getFechaPago().toString());
                table.addCell(String.format("%.2f", pago.getMontoCuota()));
                table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Mostrar el pago de intereses
                table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Mostrar la amortización
                table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Mostrar el saldo restante
                table.addCell(pago.getEstado());
            }

            document.add(table);

            // Cierre del documento
            document.close();

            // Retornar el PDF como un byte array
            return baos.toByteArray();
        }

    }

    private List<CronogramaPagos> generarCronograma(Prestamo prestamo) {
        List<CronogramaPagos> cronograma = new ArrayList<>();
        double cuotaMensual = calcularMontoCuota(prestamo);
        double saldoRestante = prestamo.getMonto();  // Inicializamos con el monto total del préstamo

        for (int i = 1; i <= prestamo.getPlazo(); i++) {
            CronogramaPagos pago = new CronogramaPagos();
            pago.setPrestamo(prestamo);
            pago.setFechaPago(LocalDate.now().plusMonths(i));
            pago.setMontoCuota(cuotaMensual);

            // Calcular los pagos de intereses, amortización y saldo restante
            double pagoIntereses = saldoRestante * (prestamo.getInteres() / 100);
            double amortizacion = cuotaMensual - pagoIntereses;
            saldoRestante -= amortizacion;

            pago.setPagoIntereses(pagoIntereses);  // Establecer el pago de intereses
            pago.setAmortizacion(amortizacion);    // Establecer la amortización
            pago.setSaldoRestante(saldoRestante);  // Establecer el saldo restante

            pago.setEstado("Pendiente");
            cronograma.add(pago);
        }

        return cronograma;
    }

    public List<CronogramaPagos> obtenerCronogramaConEstadosActualizados(Prestamo prestamo) {
        List<CronogramaPagos> cronograma = prestamo.getCronogramaPagos();
        for (CronogramaPagos pago : cronograma) {
            if (pago.getEstado().equals("Pendiente") && pago.getFechaPago().isBefore(LocalDate.now())) {
                pago.setEstado("Deuda");
                cronogramaPagosRepository.save(pago);
            }
        }
        return cronograma;
    }

    public void marcarComoPagado(Long id) {
        CronogramaPagos pago = cronogramaPagosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado("Pagado");
        cronogramaPagosRepository.save(pago);
    }



    private double calcularMontoCuota(Prestamo prestamo) {
        double tasaMensual = 0;

        // Condiciones para el plazo del préstamo
        if (prestamo.getPlazo() == 1) {
            // Convertimos la TEA del 10% a mensual
            tasaMensual = Math.pow(1 + 0.10, 1.0 / 12) - 1; // 0.007974
        } else if (prestamo.getPlazo() == 6) {
            // Convertimos la TEA del 20% a mensual
            tasaMensual = Math.pow(1 + 0.20, 1.0 / 12) - 1; // 0.01541
        }

        // Redondear la tasa mensual a 6 decimales usando BigDecimal
        BigDecimal tasaMensualRedondeada = new BigDecimal(tasaMensual).setScale(6, RoundingMode.HALF_UP);

        prestamo.setInteres(tasaMensualRedondeada.doubleValue() * 100);

        // Cálculo del total a pagar con la tasa mensual
        double totalPagar = prestamo.getMonto() * (1 + tasaMensualRedondeada.doubleValue() * prestamo.getPlazo());

        // Calculamos la cuota mensual
        return totalPagar / prestamo.getPlazo();
    }

    public List<Prestamo> findByClienteNroDocumento(String nro){
        return prestamoRepository.findByCliente_NroDocumento(nro);
    }

}
