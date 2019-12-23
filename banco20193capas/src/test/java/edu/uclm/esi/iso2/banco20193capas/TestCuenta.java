package edu.uclm.esi.iso2.banco20193capas;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.MovimientoCuenta;
import edu.uclm.esi.iso2.banco20193capas.model.MovimientoTarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoAutorizadoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoEncontradoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaInvalidaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaSinTitularesException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaYaCreadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TarjetaBloqueadaException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Tarjeta;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaDebito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCuenta extends TestCase {
	
	@Before
	public void setUp() {
		Manager.getMovimientoDAO().deleteAll();
		Manager.getMovimientoTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaDebitoDAO().deleteAll();
		Manager.getCuentaDAO().deleteAll();
		Manager.getClienteDAO().deleteAll();
	}
	
	@Test
	public void testRetiradaSinSaldo() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(1000);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e);
		}
		try {
			cuentaPepe.retirar(2000);
			fail("Esperaba SaldoInsuficienteException");
		} catch (ImporteInvalidoException e) {
			fail("Se ha producido ImporteInvalidoException");
		} catch (SaldoInsuficienteException e) {
		}
	}
	
	@Test
	public void testCreacionDeUnaCuenta() {
		try {
			Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
			pepe.insert();
			
			Cuenta cuentaPepe = new Cuenta(1);
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(1000);
			assertTrue(cuentaPepe.getSaldo()==1000);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testNoCreacionDeUnaCuenta() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		
		try {
			cuentaPepe.insert();
			fail("Esperaba CuentaSinTitularesException");
		} catch (CuentaSinTitularesException e) {
		}
	}
	
	@Test
	public void testTransferencia() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cliente ana = new Cliente("98765F", "Ana", "López");
		ana.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		Cuenta cuentaAna = new Cuenta(2);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaAna.addTitular(ana);
			cuentaAna.insert();
			
			cuentaPepe.ingresar(1000);
			assertTrue(cuentaPepe.getSaldo()==1000);
			
			cuentaPepe.transferir(cuentaAna.getId(), 500, "Alquiler");
			assertTrue(cuentaPepe.getSaldo() == 495);
			assertTrue(cuentaAna.getSaldo() == 500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraConTC() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			tc.comprar(tc.getPin(), 300);
			assertTrue(tc.getCreditoDisponible()==700);
			tc.liquidar();
			assertTrue(tc.getCreditoDisponible()==1000);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testTransferenciaOK() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cliente ana = new Cliente("98765K", "Ana", "López");
		ana.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		Cuenta cuentaAna = new Cuenta(2);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(1000);
			cuentaAna.addTitular(ana);
			cuentaAna.insert();
			cuentaPepe.transferir(2L, 500, "Alquiler");
			assertTrue(cuentaPepe.getSaldo()==495);
			assertTrue(cuentaAna.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada");
		}
	}
	
	@Test
	public void testTransferenciaALaMismaCuenta() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(1000);
			cuentaPepe.transferir(1L,100, "Alquiler");
			fail("Esperaba CuentaInvalidaException");
		} catch (CuentaInvalidaException e) {
		} catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
	}
	
	@Test
	public void testCompraPorInternetConTC() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			int token = tc.comprarPorInternet(tc.getPin(), 300);
			assertTrue(tc.getCreditoDisponible()==1000);
			tc.confirmarCompraPorInternet(token);
			assertTrue(tc.getCreditoDisponible()==700);
			tc.liquidar();
			assertTrue(tc.getCreditoDisponible()==1000);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarTitularCuenta(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		Cliente jose = new Cliente("76542G", "Jose", "Perez");
		jose.insert();
		try{
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			List<Cliente> NuevoTitular = new ArrayList<>();
			NuevoTitular.add(jose);
			cuentaPepe.setTitulares(NuevoTitular);
			assertTrue(cuentaPepe.getTitulares().equals(NuevoTitular));
		}catch(Exception e){
			fail("Excepcion inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testRetiroForzoso(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try{
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(10000);
			cuentaPepe.retiroForzoso(1000, "Comunidad");
			assertTrue(cuentaPepe.getSaldo()==9000);
		}catch(Exception e){
			fail("Excepcion inesperada: " + e.getMessage());
		}
	}
	@Test
	public void testRetiroForzosoSinDinero(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try{
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.ingresar(500);
			cuentaPepe.retiroForzoso(1000, "Comunidad");
			assertTrue(cuentaPepe.getSaldo()==-500);
		}catch(Exception e){
			fail("Excepcion inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraConTD() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("12345X");
			td.comprar(td.getPin(), 300);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraPorInternetConTD() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("12345X");
			int token = td.comprarPorInternet(td.getPin(), 300);
			td.confirmarCompraPorInternet(token);
			assertTrue(cuentaPepe.getSaldo()==500);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarID() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaPepe.setCreada(true);
			assertTrue(cuentaPepe.isCreada());
			cuentaPepe.setId((long)2);
			assertTrue(cuentaPepe.getId()==2);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testSacarDineroTC() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			tc.sacarDinero(tc.getPin(), 300);
			assertTrue(tc.getCreditoDisponible()==697);
			tc.liquidar();
			assertTrue(tc.getCreditoDisponible()==1000);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarPinTC() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			tc.cambiarPin(tc.getPin(), 1234);
			assertTrue(tc.getPin()==1234);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testModificarTarjeta() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X",1000);
			tc.setId((long)2);
			tc.setPin(1234);
			tc.setTitular(pepe);
			tc.setActiva(false);
			assertTrue(tc.getCuenta()== cuentaPepe);
			assertTrue(tc.getTitular()==pepe);
			assertTrue(tc.getId()==2);
			assertTrue(tc.getPin()==1234);
			assertFalse(tc.isActiva());
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testMovimientoTCAtributos() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X", 1000);
			TarjetaCredito tc1 = cuentaPepe.emitirTarjetaCredito("12345X", 2000);
			MovimientoTarjetaCredito mtc = new MovimientoTarjetaCredito(tc,200,"Alquiler");
			mtc.setId((long)2);
			mtc.setTarjeta(tc1);
			mtc.setImporte(300);
			mtc.setConcepto("Luz");
			assertTrue(mtc.getTarjeta() == tc1);
			assertTrue(mtc.getImporte() == 300);
			assertTrue(mtc.getId()==2);
			assertTrue(mtc.getConcepto()== "Luz");
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCuentaAtributos() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		Cliente ana = new Cliente("98765K", "Ana", "López");
		ana.insert();
		Cuenta cuentaAna = new Cuenta(2);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaAna.addTitular(ana);
			cuentaAna.insert();
			MovimientoCuenta mc = new MovimientoCuenta(cuentaPepe,200,"Alquiler");
			mc.setId((long)2);
			mc.setCuenta(cuentaAna);
			mc.setImporte(300);
			mc.setConcepto("Luz");
			assertTrue(mc.getCuenta() == cuentaAna);
			assertTrue(mc.getImporte() == 300);
			assertTrue(mc.getId()==2);
			assertTrue(mc.getConcepto()== "Luz");
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testMovimientoCuentaAtributos() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			pepe.setId((long)2);
			pepe.setNif("67890Y");
			pepe.setNombre("Pedro");
			pepe.setApellidos("Picapiedra");
			assertTrue(pepe.getNombre() == "Pedro");
			assertTrue(pepe.getApellidos() == "Picapiedra");
			assertTrue(pepe.getId()==2);
			assertTrue(pepe.getNif()== "67890Y");
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testAtributos() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		Cliente ana = new Cliente("98765K", "Ana", "López");
		ana.insert();
		Cuenta cuentaAna = new Cuenta(2);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			cuentaAna.addTitular(ana);
			cuentaAna.insert();
			MovimientoCuenta mc = new MovimientoCuenta(cuentaPepe,200,"Alquiler");
			mc.setId((long)2);
			mc.setCuenta(cuentaAna);
			mc.setImporte(300);
			mc.setConcepto("Luz");
			assertTrue(mc.getCuenta() == cuentaAna);
			assertTrue(mc.getImporte() == 300);
			assertTrue(mc.getId()==2);
			assertTrue(mc.getConcepto()== "Luz");
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarPinTD() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("12345X");
			td.cambiarPin(td.getPin(), 1234);
			assertTrue(td.getPin()==1234);
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testBloquearCuentaDebito(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("12345X");
			td.sacarDinero(9852,200);
			td.sacarDinero(9852, 200);
			td.sacarDinero(9852, 200);
			fail("Se esperaba PinInvalido exception");
		} catch(PinInvalidoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testBloquearTD(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("12345X");
			td.setActiva(false);
			td.sacarDinero(td.getPin(),200);
			fail("Se esperaba TarjetaBloqueadaException");
		} catch(TarjetaBloqueadaException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testClienteNoEncontradoTD(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("67890Y");
			fail("Se esperaba ClienteNoEncontradoException");
		} catch(ClienteNoEncontradoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testClienteNoEncontradoTC(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("67890Y",1000);
			fail("Se esperaba ClienteNoEncontradoException");
		} catch(ClienteNoEncontradoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testClienteNoAutorizadoTC(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		Cliente ana = new Cliente("98765K", "Ana", "López");
		ana.insert();
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("98765K",1000);
			int token = tc.comprarPorInternet(tc.getPin(), 200);
			fail("Se esperaba ClienteNoAutorizadoException");
		} catch(ClienteNoAutorizadoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testClienteNoAutorizadoTD(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		Cliente ana = new Cliente("98765K", "Ana", "López");
		ana.insert();
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			
			TarjetaDebito td = cuentaPepe.emitirTarjetaDebito("98765K");
			int token = td.comprarPorInternet(td.getPin(), 200);
			fail("Se esperaba ClienteNoAutorizadoException");
		} catch(ClienteNoAutorizadoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testSaldoInsuficienteTC(){
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		try {
			cuentaPepe.addTitular(pepe);
			cuentaPepe.insert();
			
			cuentaPepe.ingresar(1000);
			cuentaPepe.retirar(200);;
			assertTrue(cuentaPepe.getSaldo()==800);
			TarjetaCredito tc = cuentaPepe.emitirTarjetaCredito("12345X",100);
			int token = tc.comprarPorInternet(tc.getPin(), 3000);
			fail("Se esperaba SaldoInsuficienteException");
		} catch(SaldoInsuficienteException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
}
