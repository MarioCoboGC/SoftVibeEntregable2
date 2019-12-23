package edu.uclm.esi.iso2.banco20193capas;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaInvalidaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TarjetaBloqueadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TokenInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.Tarjeta;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;

public class TestTarjeta {
	
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
	public void testNoIsActiva() {
		
		Cliente juan = new Cliente("12346X", "Pepe", "Pérez");
		juan.insert();
		
		Cuenta cuentaJuan = new Cuenta(1);
		try {
			cuentaJuan.addTitular(juan);
			cuentaJuan.insert();
			TarjetaCredito tc = cuentaJuan.emitirTarjetaCredito("12346X", 1000);
			int token = tc.comprarPorInternet(tc.getPin(), 300);
			tc.setActiva(false);
			tc.confirmarCompraPorInternet(token);
			fail("Esperaba TarjetaBloqueadaException");
		} catch(TarjetaBloqueadaException e ) {
			
		}catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
	}
	
	@Test
	public void testTokenInvalidoTarjeta() {
		Cliente juan = new Cliente("12346X", "Pepe", "Pérez");
		juan.insert();
		Cuenta cuentaJuan = new Cuenta(1);

		try {
			cuentaJuan.addTitular(juan);
			cuentaJuan.insert();
			TarjetaCredito tc = cuentaJuan.emitirTarjetaCredito("12346X", 1000);
			int token = tc.comprarPorInternet(tc.getPin(), 300);
			tc.confirmarCompraPorInternet(0);
			fail("Esperaba TokenInvalidoException");
		} catch(TokenInvalidoException e ) {
			
		}catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
	}
	
	@Test
	public void testImporteInvalidoCompraTarjeta() {
		Cliente juan = new Cliente("12346X", "Pepe", "Pérez");
		juan.insert();
		Cuenta cuentaJuan = new Cuenta(1);

		try {
			cuentaJuan.addTitular(juan);
			cuentaJuan.insert();
			TarjetaCredito tc = cuentaJuan.emitirTarjetaCredito("12346X", 1000);
			int token = tc.comprarPorInternet(tc.getPin(), 0);
			tc.confirmarCompraPorInternet(token);
			fail("Esperaba ImporteInvalidoException");
		} catch(ImporteInvalidoException e ) {
			
		}catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
		
	}
	
	@Test
	public void testSaldoInsuficienteCompraTarjeta() {
		Cliente juan = new Cliente("12346X", "Pepe", "Pérez");
		juan.insert();
		Cuenta cuentaJuan = new Cuenta(1);

		try {
			cuentaJuan.addTitular(juan);
			cuentaJuan.insert();
			TarjetaCredito tc = cuentaJuan.emitirTarjetaCredito("12346X", 1000);
			int token = tc.comprarPorInternet(tc.getPin(), 2000);
			tc.confirmarCompraPorInternet(token);
			fail("Esperaba SaldoInsuficienteException");
		} catch(SaldoInsuficienteException e ) {
			
		}catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
		
	}
	
	@Test 
	public void testPinInvalidoCompraTarjeta() {
		Cliente juan = new Cliente("12346X", "Pepe", "Pérez");
		juan.insert();
		Cuenta cuentaJuan = new Cuenta(1);

		try {
			cuentaJuan.addTitular(juan);
			cuentaJuan.insert();
			TarjetaCredito tc = cuentaJuan.emitirTarjetaCredito("12346X", 1000);
			tc.cambiarPin(0, 1234);
			int token = tc.comprarPorInternet(6789, 2000);
			tc.confirmarCompraPorInternet(token);
			fail("Esperaba PinInvalidoException");
		} catch(PinInvalidoException e ) {
			
		}catch (Exception e) {
			fail("Se ha lanzado una excepción inesperada: " + e);
		}
		
	}
	
}
	
