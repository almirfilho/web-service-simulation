package teste;

import simulation.WebService;

public class TestDrive {

	public static void main(String[] args) {

		double tempo = Double.parseDouble( args[0] );
		double taxaReq = Double.parseDouble( args[1] );
		int numServ = Integer.parseInt( args[2] );
		
		WebService service = new WebService( tempo, taxaReq, numServ );
		service.simulate();
	}

}