package ch.pschatzmann.jflightcontroller4pi.tuning;

import java.util.Arrays;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.devices.IOutDeviceEx;
import ch.pschatzmann.jflightcontroller4pi.devices.OutDevice;
import ch.pschatzmann.jflightcontroller4pi.imu.IMUDevice;
import ch.pschatzmann.jflightcontroller4pi.integration.DatagramReader;
import ch.pschatzmann.jflightcontroller4pi.integration.GraphiteMetrics;
import ch.pschatzmann.jflightcontroller4pi.modes.PIDModeRule;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;
import ch.pschatzmann.jflightcontroller4pi.protocols.NullDevice;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

/**
 * We try to use a genetic algorythm to determine the best P,I and D settings
 * for the aileron and elevator using Flightgear as flight simulator:
 * 
 * For that we just try to fly as streight ahead as possible for 1 minute with 0
 * pitch and 0 roll.
 * 
 * @author pschatzmann
 *
 */

public class PIDTuner {
	private static Logger log = LoggerFactory.getLogger(PIDTuner.class);
	private double pitchError;
	private long pitchCount;
	private double rollError;
	private int rollCount;
	private PIDError tuneError;
	private FlightController ctl;
	private FlightgearLauncher laucher;
	private HeartBeatSensor heartBeatSensor;

	/**
	 * The PIDTuner starts here...
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PIDTuner t = new PIDTuner();
		t.setup();
		Genotype<DoubleGene> result = t.evaluate();
		System.out.println("Best result:" + result);
		System.exit(0);
	}

	protected Genotype<DoubleGene> evaluate() {
		// 1.) Define the genotype (factory) suitable
		// for the problem.
		Factory<Genotype<DoubleGene>> gtf = Genotype.of(DoubleChromosome.of(0, 10, 100), DoubleChromosome.of(0, 0.9, 100), DoubleChromosome.of(0, 0.9, 100),
				DoubleChromosome.of(0, 10, 100), DoubleChromosome.of(0, 0.9, 100), DoubleChromosome.of(0, 0.1, 100));

		// 3.) Create the execution environment.
		Engine<DoubleGene, Double> engine = Engine.builder(this::eval, gtf).build();

		// 4.) Start the execution (evolution) and
		// collect the result.
		Genotype<DoubleGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());
		return result;
	}

	protected void setup() {
		ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
		ctl = (FlightController) context.getBean("flightController");
		laucher = (FlightgearLauncher) context.getBean("flightgearLauncher");
		heartBeatSensor = new HeartBeatSensor();
		ctl.addDevices(Arrays.asList(heartBeatSensor));
		ctl.selectMode("stabilizedMode");
		tuneError = new PIDError(ctl);

		new Thread(() -> {
			ctl.run();
		}).start();

	}

	public synchronized  Double eval(Genotype<DoubleGene> gt) {
		log.info("eval");

		IOutDeviceEx elevator = ctl.getControlDevice(ParametersEnum.ELEVATOR);
		PIDModeRule ruleElevaor = (PIDModeRule) elevator.getRecalculate();
		IOutDeviceEx aileron = ctl.getControlDevice(ParametersEnum.AILERON);
		PIDModeRule ruleAileron = (PIDModeRule) aileron.getRecalculate();

		DoubleChromosome dc = gt.getChromosome().as(DoubleChromosome.class);
		ruleElevaor.setP(dc.doubleValue(0));
		ruleElevaor.setI(dc.doubleValue(1));
		ruleElevaor.setD(dc.doubleValue(2));
		ruleElevaor.setup();

		ruleAileron.setP(dc.doubleValue(3));
		ruleAileron.setI(dc.doubleValue(4));
		ruleAileron.setD(dc.doubleValue(5));
		ruleAileron.setup();

		// set target
		ctl.setValue(ParametersEnum.PITCH, 0);
		ctl.setValue(ParametersEnum.ROLL, 0);
		ctl.setValue(ParametersEnum.THROTTLE, 0.8);

		double fitness = Double.MIN_VALUE;
		while (true) {
			try {
				fitness = tryEval();
				break;
			} catch (Exception ex) {
				log.error("Eval has failed: " + ex.getMessage() + " - we retry...");
			}
		}

		StringBuilder sbuf = new StringBuilder();
		Formatter fmt = new Formatter(sbuf);
		fmt.format("fitness: %f <= elevator: %s / aileron: %s", fitness, ruleElevaor.toString(), ruleAileron.toString());
		System.out.println(sbuf.toString());
		fmt.close();

		return fitness;
	}

	protected double tryEval() throws Exception {
		log.info("tryEval");
		// restart flightgear
		laucher.start();

		// reset the error
		ctl.sleep(1000);
		tuneError.reset();

		// evaluate for 1 minute
		for (int j = 0; j < 600; j++) {
			if (!heartBeatSensor.isActive()) {
				throw new Exception("We did not get any telemetry data: Flightgear might have crashed");
			}
			ctl.sleep(100);
		}
		double fitness = tuneError.getFitness();
		return fitness;
	}

}
