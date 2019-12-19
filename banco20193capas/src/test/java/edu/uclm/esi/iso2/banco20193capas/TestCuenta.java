package edu.uclm.esi.iso2.banco20193capas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoAutorizadoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoEncontradoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaInvalidaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaSinTitularesException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaYaCreadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TokenInvalidoException;
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
	/*@Test
	public void testErrorCompra() {
		Cliente pepe = new Cliente("12345X", "Pepe", "Pérez");
		pepe.insert();
		Cuenta cuentaPepe = new Cuenta(1);
		
		try {
			cuentaPepe.addTitular(pepe);
		} catch (CuentaYaCreadaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cuentaPepe.insert();
		} catch (CuentaSinTitularesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			cuentaPepe.ingresar(1000);
		} catch (ImporteInvalidoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TarjetaDebito td;
		try {
			td = cuentaPepe.emitirTarjetaDebito("12345X");
			try {
				int token=td.comprarPorInternet(td.getPin(), 100);
				td.confirmarCompraPorInternet(token);
			}catch(TokenInvalidoException e) {
			}catch (Exception e) {
				// TODO: handle exception
			}	
		} catch (ClienteNoEncontradoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClienteNoAutorizadoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
			
			
		}*/
}
	

