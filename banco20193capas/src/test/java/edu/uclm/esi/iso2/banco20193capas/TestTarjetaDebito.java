package edu.uclm.esi.iso2.banco20193capas;

import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.exceptions.TokenInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaDebito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTarjetaDebito extends TestCase {
	
		  private Cuenta cuentaPepe, cuentaAna;
		  private Cliente pepe, ana;
		  private TarjetaDebito tdPepe, tdAna;
		  private TarjetaCredito tcPepe, tcAna;
		  
		  @Before(value = "")
		  public void setUp() {
		    Manager.getMovimientoDAO().deleteAll();
		    Manager.getMovimientoTarjetaCreditoDAO().deleteAll();
		    Manager.getTarjetaCreditoDAO().deleteAll();
		    Manager.getTarjetaDebitoDAO().deleteAll();
		    Manager.getCuentaDAO().deleteAll();
		    Manager.getClienteDAO().deleteAll();

		    this.pepe = new Cliente("12345X", "Pepe", "Pérez");
		    this.pepe.insert();
		    this.ana = new Cliente("98765F", "Ana", "López");
		    this.ana.insert();
		    this.cuentaPepe = new Cuenta(1);
		    this.cuentaAna = new Cuenta(2);
		    
		    
		    try {
		      this.cuentaPepe.addTitular(pepe);
		      this.cuentaPepe.insert();
		      this.cuentaPepe.ingresar(1000);
		      this.cuentaAna.addTitular(ana);
		      this.cuentaAna.insert();
		      this.cuentaAna.ingresar(5000);
		      this.tcPepe = this.cuentaPepe.emitirTarjetaCredito(pepe.getNif(), 2000);
		      this.tcPepe.cambiarPin(this.tcPepe.getPin(), 1234);
		      this.tcAna = this.cuentaAna.emitirTarjetaCredito(ana.getNif(), 10000);
		      this.tcAna.cambiarPin(this.tcAna.getPin(), 1234);
		      this.tdPepe = this.cuentaPepe.emitirTarjetaDebito(pepe.getNif());
		      this.tdPepe.cambiarPin(this.tdPepe.getPin(), 1234);
		      this.tdAna = this.cuentaAna.emitirTarjetaDebito(ana.getNif());
		      this.tdAna.cambiarPin(this.tdAna.getPin(), 1234);
		    } catch (Exception e) {
		      fail("Excepción inesperada en setUp(): " + e);
		    }
		  }
		  
	@Test
	public void testPruebaTarjetaDebito() {
		try {
			int token=tdPepe.comprarPorInternet(tdPepe.getPin(), 100);
			tdPepe.confirmarCompraPorInternet(token);
		}catch(TokenInvalidoException e) {
		}catch (Exception e) {
			// TODO: handle exception
			
		}
		
	}
		  

}
