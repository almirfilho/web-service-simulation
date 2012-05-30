package simulation;

import java.util.Random;

import src.Const;
import src.Sim;
import src.SimEvent;

public class WebService {
	
	/** ------------------------------------------
	 * Estados da Simulação
	 -------------------------------------------*/
	
	private static final int
		BEGIN = 1,
		REQUEST_LINK_IN = 2,
		RELEASE_LINK_IN = 3,
		REQUEST_ROUTER_IN = 4,
		RELEASE_ROUTER_IN = 5,
		REQUEST_ROUTER_OUT = 20,
		RELEASE_ROUTER_OUT = 21,
		REQUEST_LAN_IN = 6,
		RELEASE_LAN_IN = 7,
		REQUEST_LAN_OUT = 22,
		RELEASE_LAN_OUT = 23,
		REQUEST_CPU_IN = 8,
		RELEASE_CPU_IN = 9,
		REQUEST_CPU_OUT = 24,
		RELEASE_CPU_OUT = 25,
		REQUEST_DISC = 10,
		RELEASE_DISC = 11,
		REQUEST_LINK_OUT = 12,
		RELEASE_LINK_OUT = 13,
		END = 14;
	
	/** ------------------------------------------
	 * Atributos
	 -------------------------------------------*/
	
	private int linkIn, router, lan, linkOut; // Facilities da rede
	
	private int[] cpus; // Facilities de CPUs dos servidores
	
	private int[] discs; // Facilities de discos dos servidores
	
	private double routerTime = 0.050, discTime = 0.006, lanTime, linkTime; // tempos de serviço/latência
	
	private int maximumSegmentSize = 1460, httpRequestSize = 290, overheadFrameSize = 18; // em bytes
	
	private double larguraBandaLan = 10.0, larguraBandaLink = 1.544; // em Mbps
	
	private int numRequisitions; // numero total de requisições
	
	private int currentRequisitionClass; // classe da requisição atual
	
	private int requisitionPackages; // quantidade de pacotes necessária para comportar arquivo requisitado
	
	private int currentServer; // servidor atual em serviço
		
	private SimEvent requisicao = new SimEvent(); // manipula a simulação
	
	private Random random = new Random(); // para gerar numeros randomicos apenas
	
	private double time;
	
	/** ------------------------------------------
	 * Construtor
	 -------------------------------------------*/
	
	public WebService( double simulationTime, double requisitionTax, int numServers ){

		this.numRequisitions = (int)( simulationTime * requisitionTax );
		this.lanTime = (8 * ( this.overheadFrameSize + this.httpRequestSize )) / (larguraBandaLan * 1024 * 1024 * 8);
		this.linkTime = (8 * ( this.overheadFrameSize + this.httpRequestSize )) / (larguraBandaLink * 1024 * 1024 * 8);
		this.cpus = new int[ numServers ];
		this.discs = new int[ numServers ];
		this.time = simulationTime;
	}
	
	/** ------------------------------------------
	 * Métodos
	 -------------------------------------------*/
	
	private void init(){

		Sim.init( 0.0, Const.LINKED );
		this.linkIn = Sim.create_facility( "Link In", 1 );
		this.router = Sim.create_facility( "Router", 1 );
		this.lan = Sim.create_facility( "LAN", 1 );
		this.linkOut = Sim.create_facility( "Link Out", 1 );
		
		for( int i = 0; i < this.cpus.length; i++ ){
			
			this.cpus[ i ] = Sim.create_facility( "CPU "+ (i+1), 1 );
			this.discs[ i ] = Sim.create_facility( "Disc "+ (i+1), 1 );
		}
		
		this.requisicao.id = BEGIN;
	}
	
	public void simulate(){
		
		this.init();
		Sim.schedule( this.requisicao, 0.0 );

		while( this.numRequisitions > 0 && Sim.time() <= this.time ){
			
			// Próximo estado da requisicao
            this.requisicao = Sim.next_event( 0.0, Const.ASYNC );

            switch( this.requisicao.id ){
            
            case BEGIN:
        		this.begin();
        		break;
        		
            case REQUEST_LINK_IN:
        		this.requestLinkIn();
        		break;
        		
            case RELEASE_LINK_IN:
        		this.releaseLinkIn();
        		break;
        		
            case REQUEST_ROUTER_IN:
        		this.requestRouter( REQUEST_ROUTER_IN );
        		break;
        		
            case RELEASE_ROUTER_IN:
        		this.releaseRouter( RELEASE_ROUTER_IN );
        		break;
        		
            case REQUEST_ROUTER_OUT:
        		this.requestRouter( REQUEST_ROUTER_OUT );
        		break;
        		
            case RELEASE_ROUTER_OUT:
        		this.releaseRouter( RELEASE_ROUTER_OUT);
        		break;
        		
            case REQUEST_LAN_IN:
        		this.requestLan( REQUEST_LAN_IN );
        		break;
        		
            case RELEASE_LAN_IN:
        		this.releaseLan( RELEASE_LAN_IN );
        		break;
        		
            case REQUEST_LAN_OUT:
        		this.requestLan( REQUEST_LAN_OUT );
        		break;
        		
            case RELEASE_LAN_OUT:
        		this.releaseLan( RELEASE_LAN_OUT );
        		break;
        		
            case REQUEST_CPU_IN:
        		this.requestCpu( REQUEST_CPU_IN );
        		break;
        		
            case RELEASE_CPU_IN:
        		this.releaseCpu( RELEASE_CPU_IN );
        		break;
        		
            case REQUEST_CPU_OUT:
        		this.requestCpu( REQUEST_CPU_OUT );
        		break;
        		
            case RELEASE_CPU_OUT:
        		this.releaseCpu( RELEASE_CPU_OUT );
        		break;
        		
            case REQUEST_DISC:
        		this.requestDisc();
        		break;
        		
            case RELEASE_DISC:
        		this.releaseDisc();
        		break;
        		
            case REQUEST_LINK_OUT:
        		this.requestLinkOut();
        		break;
        		
            case RELEASE_LINK_OUT:
        		this.releaseLinkOut();
        		break;
        		
            case END:
        		this.end();
        		break;
            }
		}
		
		Sim.report_stats();
		Sim.returnUtil();
	}
	
	private int randomClass(){
		
		int random = Sim.random( 1, 100 );
		
		if( random <= 1 )
			return 4;
		
		else if( random <= 15 )
			return 3;
		
		else if( random <= 65 )
			return 2;
		
		else
			return 1;
	}
	
	private int calculatePackages(){
		
		double size = 0;
		
		switch( this.currentRequisitionClass ){
		
		case 1:
			size = 5 * 1024; // 5Kb
			break;
			
		case 2:
			size = 10 * 1024; // 10Kb
			break;
			
		case 3:
			size = 38.5 * 1024; // 38,5Kb
			break;
			
		case 4:
			size = 350 * 1024; // 350Kb
			break;
		}
		
		if( size == 0 )
			return 1;
		
		return (int) Math.ceil( size / this.maximumSegmentSize );
	}
	
	private double netFileTime( double tamanhoDoc, double larguraBanda ){
		
        double overhead = this.overheadFrameSize * this.requisitionPackages;
        return (8 * (tamanhoDoc + overhead)) / (larguraBanda * 1024 * 1024 * 8);
    }
	
	private double fileSize(){
		
		switch( this.currentRequisitionClass ){
		
		case 1:
			return 5.0 * 1024;
			
		case 2:
			return 10.0 * 1024;
			
		case 3:
			return 38.5 * 1024;
			
		case 4:
			return 350.0 * 1024;
			
		default:
			return 0.0;
		}
	}
	
	private int selectServer(){
		
		this.currentServer = this.random.nextInt( this.cpus.length );
		return this.currentServer;
	}
	
	/** ------------------------------------------
	 * Métodos de estados
	 -------------------------------------------*/
	
	private void begin(){
		
		this.currentRequisitionClass = this.randomClass();
		this.requisitionPackages = this.calculatePackages();
		this.requisicao.id = REQUEST_LINK_IN;
		Sim.schedule( requisicao, 0.0 );
		Sim.update_arrivals();
	}
	
	private void requestLinkIn(){
		
		if( Sim.request( this.linkIn, this.requisicao.token, 0 ) == Const.FREE ) {
            this.requisicao.id = RELEASE_LINK_IN;
            Sim.schedule( this.requisicao, this.netFileTime( this.fileSize(), this.larguraBandaLink ) );
        }
	}
	
	private void releaseLinkIn(){
		
		this.requisicao.id = REQUEST_ROUTER_IN;
        Sim.release( this.linkIn, this.requisicao.token);
        Sim.schedule( this.requisicao, 0.0 );
	}
	
	private void requestRouter( int state ){
		
		if( Sim.request( this.router, this.requisicao.token, 0 ) == Const.FREE ) {
			
			if( state == REQUEST_ROUTER_IN )
				this.requisicao.id = RELEASE_ROUTER_IN;
			
			else if( state == REQUEST_ROUTER_OUT )
				this.requisicao.id = RELEASE_ROUTER_OUT;
			
            Sim.schedule( this.requisicao, this.routerTime );
        }
	}
	
	private void releaseRouter( int state ){
		
		Sim.release( this.router, this.requisicao.token );
		
		if( state == RELEASE_ROUTER_IN ){
			
			this.requisicao.id = REQUEST_LAN_IN;
			Sim.schedule( this.requisicao, 0.0 );
		
		} else if( state == RELEASE_ROUTER_OUT ){
			
			this.requisicao.id = REQUEST_LINK_OUT;
			Sim.schedule( this.requisicao, this.netFileTime( this.fileSize(), this.larguraBandaLink ) );
		}
	}
	
	private void requestLan( int state ){
		
		if( Sim.request( this.lan, this.requisicao.token, 0 ) == Const.FREE ) {
			
			if( state == REQUEST_LAN_IN ){
				
				this.requisicao.id = RELEASE_LAN_IN;
				Sim.schedule( this.requisicao, this.netFileTime( this.fileSize(), this.larguraBandaLan ) );
				
			} else if( state == REQUEST_LAN_OUT ){
				
				this.requisicao.id = RELEASE_LAN_OUT;
				Sim.schedule( this.requisicao, this.netFileTime( this.fileSize(), this.larguraBandaLan ) );
			}
        }
	}
	
	private void releaseLan( int state ){
		
		if( state == RELEASE_LAN_IN )
			this.requisicao.id = REQUEST_CPU_IN;
		
		else if( state == RELEASE_LAN_OUT )
			this.requisicao.id = REQUEST_ROUTER_OUT;
		
        Sim.release( this.lan, this.requisicao.token );
        Sim.schedule( this.requisicao, 0.0 );
	}
	
	private void requestCpu( int state ){
		
		if( Sim.request( this.cpus[ this.selectServer() ], this.requisicao.token, 0 ) == Const.FREE ) {
			
			// tempo do cpu dependendo do tipo de requisicao
			double cpuTime = 0.0;
			
			if( state == REQUEST_CPU_IN ){
				
				this.requisicao.id = RELEASE_CPU_IN;
				
				switch( this.currentRequisitionClass ){
				
				case 1:
					cpuTime = 0.00645;
					break;
					
				case 2:
					cpuTime = 0.00816;
					break;
					
				case 3:
					cpuTime = 0.01955;
					break;
					
				case 4:
					cpuTime = 0.14262;
					break;
				}
			}
			
			else if( state == REQUEST_CPU_OUT )
				this.requisicao.id = RELEASE_CPU_OUT;
			
			
            Sim.schedule( this.requisicao, cpuTime );
        }
	}
	
	private void releaseCpu( int state ){
		
		Sim.release( this.cpus[ this.currentServer ], this.requisicao.token );
		
		if( state == RELEASE_CPU_IN )	
			this.requisicao.id = REQUEST_DISC;
		
		else if( state == RELEASE_CPU_OUT )
			this.requisicao.id = REQUEST_LAN_OUT;
		
		Sim.schedule( this.requisicao, 0.0 );
	}
	
	private void requestDisc(){
		
		if( Sim.request( this.discs[ this.currentServer ], this.requisicao.token, 0 ) == Const.FREE ) {
            this.requisicao.id = RELEASE_DISC;
            Sim.schedule( this.requisicao, this.discTime * this.requisitionPackages );
        }
	}
	
	private void releaseDisc(){
		
		this.requisicao.id = REQUEST_CPU_OUT;
        Sim.release( this.discs[ this.currentServer ], this.requisicao.token);
        Sim.schedule( this.requisicao, 0.0 );
	}
	
	private void requestLinkOut(){
		
		if( Sim.request( this.linkOut, this.requisicao.token, 0 ) == Const.FREE ) {
            this.requisicao.id = RELEASE_LINK_OUT;
            Sim.schedule( this.requisicao, this.netFileTime( this.fileSize(), this.larguraBandaLink ) );
        }
	}
	
	private void releaseLinkOut(){
		
		this.requisicao.id = END;
        Sim.release( this.linkOut, this.requisicao.token);
        Sim.schedule( this.requisicao, 0.0 );
	}
	
	private void end(){
		
		Sim.update_completions();
        this.requisicao.id = BEGIN;
        Sim.schedule( this.requisicao, 0.0 );
        --this.numRequisitions;
	}
	
}