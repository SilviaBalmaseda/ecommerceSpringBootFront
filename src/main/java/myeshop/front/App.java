package myeshop.front;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import myeshop.backend.BackendApplication;
import myeshop.backend.model.*;
import myeshop.backend.repository.*;

public class App {

    public static void main(String[] args) {
        // 1. ARRANCAR EL MOTOR DE SPRING (Carga la base de datos y repositorios)
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        
        System.out.println("--- INICIANDO PRUEBA DE FLUJO COMPLETO ---");

        // 2. OBTENER LOS REPOSITORIOS (Spring nos da las instancias ya listas)
        ClienteRepository clienteRepo = context.getBean(ClienteRepository.class);
        ArticuloRepository articuloRepo = context.getBean(ArticuloRepository.class);
        CompraRepository compraRepo = context.getBean(CompraRepository.class);
        ArticuloCompraRepository lineaRepo = context.getBean(ArticuloCompraRepository.class);

        try {
            // --- PASO 3: CREAR Y GUARDAR UN CLIENTE ---
            System.out.println("\n--- CREANDO CLIENTE ---");
            String nif = "11122233T";
            
            Cliente cli = new Cliente();
            cli.setNifCif(nif);
            cli.setNombreCompleto("Cliente Spring Boot");
            cli.setEmail("cliente.spring@example.com");
            cli.setFechaRegistro(LocalDateTime.now());
            
            InformacionFiscal info = new InformacionFiscal();
            info.setNifCif(nif); 
            info.setTelefono("600111222");
            info.setDireccionFiscal("Calle Nube 1");
            
            cli.setInformacionFiscal(info); // Relación 1 a 1
            
            clienteRepo.save(cli); // ¡Spring hace el persist y commit solo!
            System.out.println("Cliente guardado correctamente.");


            // --- PASO 4: CREAR ARTÍCULOS ---
            System.out.println("\n--- CREANDO ARTÍCULOS ---");
            Articulo art1 = new Articulo();
            art1.setNombre("Monitor 24 pulg");
            art1.setPrecioActual(new BigDecimal("150.00"));
            art1.setStock(10);
            
            // Guardamos y recuperamos para tener el ID autogenerado
            art1 = articuloRepo.save(art1); 
            System.out.println("Artículo creado con ID: " + art1.getId());


            // --- PASO 5: CREAR UNA COMPRA ---
            System.out.println("\n--- TRAMITANDO COMPRA ---");
            Compra compra = new Compra();
            compra.setCliente(cli);
            compra.setFechaCompra(LocalDateTime.now());
            compra.setEstado("PENDIENTE");
            compra.setDireccionEntrega("Calle de Entrega 5");
            
            // Guardamos la cabecera primero
            compra = compraRepo.save(compra);
            
            // Crear línea de detalle
            int cantidad = 2;
            BigDecimal precioLinea = art1.getPrecioActual().multiply(new BigDecimal(cantidad));
            
            ArticuloCompra linea = new ArticuloCompra(art1, compra, cantidad, precioLinea);
            lineaRepo.save(linea); // Guardamos la línea
            
            // Actualizar total compra
            compra.setPrecioTotal(precioLinea); // Simplificado para el ejemplo
            compraRepo.save(compra); // Update automático
            
            System.out.println("Compra generada ID: " + compra.getId() + " Total: " + compra.getPrecioTotal());


            // --- PASO 6: LEER DATOS (CONSULTA) ---
            System.out.println("\n--- CONSULTANDO ---");
            Optional<Cliente> lectura = clienteRepo.findById(nif);
            lectura.ifPresent(c -> System.out.println("Leído de DB: " + c.getNombreCompleto()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerramos Spring al terminar
            // context.close(); 
        }
    }
}
