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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrestamoService {
    @Autowired
    private ClienteRepository clienteRepository;


    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CronogramaPagosRepository cronogramaPagosRepository;



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


    public void crearPrestamo(String clienteId, double monto, int plazo, double interes) {
        // Obtener cliente
        Cliente cliente = clienteRepository.findByNroDocumento(clienteId);

        // Obtener el total de préstamos activos del cliente en el mes actual
        YearMonth ahora = YearMonth.now();
        Double totalPrestamosMes = prestamoRepository.obtenerTotalPrestamosMensuales(cliente.getId(), ahora.getYear(), ahora.getMonthValue());

        if (totalPrestamosMes == null) {
            totalPrestamosMes = 0.0; // Valor predeterminado si no hay datos
        }

        // Obtener todos los préstamos activos del cliente (sin importar el mes, solo los que están activos)
        List<Prestamo> prestamosActivos = prestamoRepository.findByClienteAndEstado(cliente, "Activo");

        double saldoPendienteCliente = 0.0;

        // Calcular el saldo pendiente total de los préstamos activos (restando los pagos realizados)
        for (Prestamo prestamo : prestamosActivos) {
            // Obtener el total de pagos realizados en este préstamo (solo pagos realizados)
            Double pagosRealizados = cronogramaPagosRepository.obtenerTotalPagosPrestamo(prestamo.getId());

            if (pagosRealizados == null) {
                pagosRealizados = 0.0;
            }

            // Calcular el saldo pendiente de este préstamo
            double saldoPendiente = prestamo.getMonto() - pagosRealizados;

            // Acumulamos el saldo pendiente de todos los préstamos activos
            saldoPendienteCliente += saldoPendiente;
        }

        // Verificar si el total de préstamos más el saldo pendiente exceden los 5000 soles
        System.out.println(totalPrestamosMes + saldoPendienteCliente + monto);
        System.out.println(totalPrestamosMes);
        System.out.println(saldoPendienteCliente);
        System.out.println( monto);
        if (saldoPendienteCliente + monto > 5000) {
            throw new RuntimeException("El cliente no puede solicitar préstamos que sumen más de 5000 soles al mes.");
        }

        // Crear el nuevo préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setNroDocumento(cliente.getNroDocumento());
        prestamo.setInteres(interes);
        prestamo.setMonto(monto);
        prestamo.setPlazo(plazo);
        prestamo.setEstado("Activo");

        // Generar cronograma de pagos
        List<CronogramaPagos> cronograma = generarCronograma(prestamo);
        for (CronogramaPagos pago : cronograma) {
            pago.setPrestamo(prestamo);
        }
        prestamo.setCronogramaPagos(cronograma);

        // Guardar el préstamo
        prestamoRepository.save(prestamo);
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

        // Establecer la fecha de pago inicial como 30 días después de la fecha del préstamo
        LocalDate fechaPrestamo = LocalDate.now();  // Fecha en la que se toma el préstamo
        LocalDate fechaPago = fechaPrestamo.plusDays(30);

        for (int i = 1; i <= prestamo.getPlazo(); i++) {
            CronogramaPagos pago = new CronogramaPagos();
            pago.setPrestamo(prestamo);
            pago.setFechaPago(fechaPago);
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

            // Incrementar la fecha de pago en 30 días para la siguiente cuota
            fechaPago = fechaPago.plusDays(30);
        }

        return cronograma;
    }

    public List<CronogramaPagos> obtenerCronogramaConEstadosActualizados(Prestamo prestamo) {
        List<CronogramaPagos> cronograma = prestamo.getCronogramaPagos();


        for (CronogramaPagos pago : cronograma) {
            if (pago.getEstado().equals("Pendiente") && pago.getFechaPago().isBefore(LocalDate.now())) {

                pago.setEstado("Deuda");


                long diasDeAtraso = ChronoUnit.DAYS.between(pago.getFechaPago(), LocalDate.now());

                double interesAcumulado = pago.getMontoCuota() * 0.01 * diasDeAtraso;


                double nuevoMontoCuota = pago.getMontoCuota() + interesAcumulado;

                // Actualizar el monto de la cuota con el interés
                pago.setMontoCuota(nuevoMontoCuota);

                // Guardar la cuota actualizada
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

    public CronogramaPagos obtenerPagoPorId(Long id) {
        return cronogramaPagosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    public byte[] generarPDF(CronogramaPagos pago) {
        // Obtener el préstamo asociado al pago
        Prestamo prestamo = pago.getPrestamo();
        if (prestamo.getCliente().getNroDocumento().length() == 8) {
            // Crear el PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título del documento
            document.add(new Paragraph("Detalle del la cuota")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            // Detalles del préstamo
            document.add(new Paragraph("Cliente: " + prestamo.getCliente().getNombre() + " " +
                    prestamo.getCliente().getApellidoPaterno() + " " + prestamo.getCliente().getApellidoMaterno()));
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

            // Agregar la fila correspondiente al pago
            table.addCell("1");  // N° de pago (si solo hay uno)
            table.addCell(pago.getFechaPago().toString());
            table.addCell(String.format("%.2f", pago.getMontoCuota()));
            table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Pago de intereses
            table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Amortización
            table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Saldo restante
            table.addCell(pago.getEstado());

            document.add(table);

            // Cierre del documento
            document.close();

            // Retornar el PDF como un byte array
            return baos.toByteArray();
        }
        else {
            // Crear el PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título del documento
            document.add(new Paragraph("Detalle del la cuota")
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

            // Agregar la fila correspondiente al pago
            table.addCell("1");  // N° de pago (si solo hay uno)
            table.addCell(pago.getFechaPago().toString());
            table.addCell(String.format("%.2f", pago.getMontoCuota()));
            table.addCell(String.format("%.2f", pago.getPagoIntereses()));  // Pago de intereses
            table.addCell(String.format("%.2f", pago.getAmortizacion()));    // Amortización
            table.addCell(String.format("%.2f", pago.getSaldoRestante()));  // Saldo restante
            table.addCell(pago.getEstado());

            document.add(table);

            // Cierre del documento
            document.close();

            // Retornar el PDF como un byte array
            return baos.toByteArray();
        }
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


    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutarActualizacionDeDeudaJudicial() {
        actualizarEstadoPrestamos();
    }


    public void actualizarEstadoPrestamos() {

        List<Prestamo> prestamos = prestamoRepository.findAll();

        for (Prestamo prestamo : prestamos) {

            List<CronogramaPagos> pagosPendientes = prestamo.getCronogramaPagos().stream()
                    .filter(pago -> "Pendiente".equals(pago.getEstado()))
                    .collect(Collectors.toList());

            if (!pagosPendientes.isEmpty()) {
                // Obtener la fecha de la última cuota pendiente
                CronogramaPagos ultimaCuotaPendiente = pagosPendientes.get(pagosPendientes.size() - 1);
                LocalDate fechaUltimaCuota = ultimaCuotaPendiente.getFechaPago();


                if (fechaUltimaCuota.plus(1, ChronoUnit.YEARS).isBefore(LocalDate.now())) {

                    prestamo.setEstado("Deuda Judicial");
                    prestamoRepository.save(prestamo);
                }
            }
        }
    }

}
