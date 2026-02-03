package myeshop.front;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import myeshop.backend.BackendApplication;
import myeshop.backend.model.*;
import myeshop.backend.repository.*;

/**
 * Programa de pruebas que arranca el contexto del backend usa
 * las utilidades del repositorio: consultas personalizadas,
 * JOIN FETCH, agregados, actualizaciones y borrados.
 *
 * <p>Diseñado para ejecución manual local
 * Imprime resultados concisos para verificar comportamiento.</p>
 *
 * @author Rafael Robles
 * @version 1.0.0
 */
public class App {

    public static void main(String[] args) {
        // Arrancar Spring Boot del backend
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);

        ClienteRepository clienteRepo = context.getBean(ClienteRepository.class);
        ArticuloRepository articuloRepo = context.getBean(ArticuloRepository.class);
        CompraRepository compraRepo = context.getBean(CompraRepository.class);
        ArticuloCompraRepository lineaRepo = context.getBean(ArticuloCompraRepository.class);
        InformacionFiscalRepository infoRepo = context.getBean(InformacionFiscalRepository.class);

        try {
            System.out.println("--- INICIANDO SUITE DE PRUEBAS ---");

            // --- Crear artículos ---
            Articulo a1 = new Articulo();
            a1.setNombre("Monitor 24 pulg");
            a1.setPrecioActual(new BigDecimal("150.00"));
            a1.setStock(20);
            a1 = articuloRepo.save(a1);

            Articulo a2 = new Articulo();
            a2.setNombre("Teclado mecánico");
            a2.setPrecioActual(new BigDecimal("70.00"));
            a2.setStock(4);
            a2 = articuloRepo.save(a2);

            // --- Crear clientes con informaci�n fiscal ---
            Cliente c1 = new Cliente();
            c1.setNifCif("11122233T");
            c1.setNombreCompleto("Cliente Spring Boot");
            c1.setEmail("cliente.spring@example.com");
            c1.setFechaRegistro(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

            InformacionFiscal i1 = new InformacionFiscal();
            i1.setTelefono("600111222");
            i1.setDireccionFiscal("Calle Nube 1");
            i1.setCliente(c1);
            c1.setInformacionFiscal(i1);
            clienteRepo.save(c1);

            Cliente c2 = new Cliente();
            c2.setNifCif("22233344A");
            c2.setNombreCompleto("Cliente Dos");
            c2.setEmail("otro@example.com");
            c2.setFechaRegistro(LocalDateTime.now().minusDays(1));

            InformacionFiscal i2 = new InformacionFiscal();
            i2.setTelefono("600333444");
            i2.setDireccionFiscal(null);
            i2.setCliente(c2);
            c2.setInformacionFiscal(i2);
            clienteRepo.save(c2);

            // --- Crear compras y l�neas ---
            Compra cp1 = new Compra();
            cp1.setCliente(c1);
            cp1.setFechaCompra(LocalDateTime.now());
            cp1.setEstado("PENDIENTE");
            cp1.setDireccionEntrega("Calle de Entrega 5");
            cp1 = compraRepo.save(cp1);

            Compra cp2 = new Compra();
            cp2.setCliente(c1);
            cp2.setFechaCompra(LocalDateTime.now().minusDays(2));
            cp2.setEstado("COMPLETADO");
            cp2.setDireccionEntrega("Otra calle 7");
            cp2 = compraRepo.save(cp2);

            ArticuloCompra L1 = new ArticuloCompra(a1, cp1, 2, a1.getPrecioActual().multiply(new BigDecimal(2)));
            lineaRepo.save(L1);
            ArticuloCompra L2 = new ArticuloCompra(a2, cp1, 1, a2.getPrecioActual());
            lineaRepo.save(L2);
            ArticuloCompra L3 = new ArticuloCompra(a1, cp2, 5, a1.getPrecioActual().multiply(new BigDecimal(5)));
            lineaRepo.save(L3);

            // actualizar totales y stock (simplificado)
            cp1.setPrecioTotal(L1.getPrecioCompra().add(L2.getPrecioCompra()));
            compraRepo.save(cp1);
            a1.setStock(a1.getStock() - (L1.getUnidades() + L3.getUnidades()));
            articuloRepo.save(a1);
            a2.setStock(a2.getStock() - L2.getUnidades());
            articuloRepo.save(a2);

            System.out.println("Datos creados: articulos=" + articuloRepo.count() + " clientes=" + clienteRepo.count() + " compras=" + compraRepo.count());

            // --- EJERCITAR CONSULTAS DEL BACKEND ---

            // ClienteRepository
            clienteRepo.findByEmail("cliente.spring@example.com").ifPresent(c -> System.out.println("findByEmail OK: " + c.getNombreCompleto()));
            System.out.println("findByNombreCompletoContainingIgnoreCase 'cliente' -> " + clienteRepo.findByNombreCompletoContainingIgnoreCase("cliente").size());
            clienteRepo.findByNifWithInformacionFiscal(c1.getNifCif()).ifPresent(c -> System.out.println("findByNifWithInformacionFiscal telefono=" + (c.getInformacionFiscal() != null ? c.getInformacionFiscal().getTelefono() : "null")));
            clienteRepo.findByNifWithCompras(c1.getNifCif()).ifPresent(c -> System.out.println("findByNifWithCompras comprasCount=" + (c.getCompras() != null ? c.getCompras().size() : 0)));
            clienteRepo.findByNifCompleto(c1.getNifCif()).ifPresent(c -> System.out.println("findByNifCompleto ok nif=" + c.getNifCif()));

            // InformacionFiscalRepository
            System.out.println("findByTelefonoContaining '600' -> " + infoRepo.findByTelefonoContaining("600").size());
            System.out.println("findByDireccionFiscalIsNotNull -> " + infoRepo.findByDireccionFiscalIsNotNull().size());
            infoRepo.findByCliente_NifCif(c2.getNifCif()).ifPresent(i -> System.out.println("findByCliente_NifCif -> telefono=" + i.getTelefono()));

            // ArticuloRepository
            System.out.println("findByNombreContainingIgnoreCase 'monitor' -> " + articuloRepo.findByNombreContainingIgnoreCase("monitor").size());
            System.out.println("findByStockLessThan 5 -> " + articuloRepo.findByStockLessThan(5).size());
            System.out.println("findAllWithCompras -> " + articuloRepo.findAllWithCompras().size());

            // CompraRepository
            System.out.println("findByCliente_NifCif(c1) -> " + compraRepo.findByCliente_NifCif(c1.getNifCif()).size());
            LocalDateTime desde = LocalDateTime.now().minusDays(3);
            LocalDateTime hasta = LocalDateTime.now().plusDays(1);
            System.out.println("findByFechaCompraBetween -> " + compraRepo.findByFechaCompraBetween(desde, hasta).size());
            System.out.println("findByEstadoIgnoreCase 'COMPLETADO' -> " + compraRepo.findByEstadoIgnoreCase("COMPLETADO").size());

            // ArticuloCompraRepository
            System.out.println("findById_CompraId(cp1) -> " + lineaRepo.findById_CompraId(cp1.getId()).size());
            System.out.println("findById_ArticuloId(a1) -> " + lineaRepo.findById_ArticuloId(a1.getId()).size());
            System.out.println("findByUnidadesGreaterThan(2) -> " + lineaRepo.findByUnidadesGreaterThan(2).size());
            System.out.println("countTotalUnidadesVendidas(a1) -> " + lineaRepo.countTotalUnidadesVendidas(a1.getId()));
            System.out.println("findAllByCompraIdWithArticulo(cp1) -> " + lineaRepo.findAllByCompraIdWithArticulo(cp1.getId()).size());

            // --- PRUEBAS DE UPDATE / DELETE ---
            cp1.setEstado("COMPLETADO");
            compraRepo.save(cp1);
            System.out.println("Estado actualizado cp1 -> " + compraRepo.findById(cp1.getId()).map(Compra::getEstado).orElse("n/a"));

            lineaRepo.delete(L2);
            System.out.println("Después delete línea L2, líneas en cp1 -> " + lineaRepo.findById_CompraId(cp1.getId()).size());

            clienteRepo.delete(c2);
            System.out.println("Cliente c2 existe tras delete -> " + clienteRepo.findById(c2.getNifCif()).isPresent());

            System.out.println("--- SUITE DE PRUEBAS FINALIZADA ---");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // context.close(); // descomentar si se desea detener el contexto
        }
    }
}