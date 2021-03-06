/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.*;
import edu.wpi.first.networktables.NetworkTableEntry;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

	public Robot() {
		try {
			frontClimbPiston = new DoubleSolenoid(1, 0);
			backClimbPiston = new DoubleSolenoid(3, 2);
			compressor = new Compressor(0);
		} catch (Exception e) {
			System.out.print("Cannot initialize all pneumatics!!!!!!!!!!!!!!!!!!!!");
			System.out.print(e.toString());
		}
	}

	Joystick controller = new Joystick(0);
	Button aButton = new JoystickButton(controller, 1),
			bButton = new JoystickButton(controller, 2),
			xButton = new JoystickButton(controller, 3),
			yButton = new JoystickButton(controller, 4),
			leftBumper = new JoystickButton(controller, 5),
			rightBumper = new JoystickButton(controller, 6),
			backButton = new JoystickButton(controller, 7),
			startButton = new JoystickButton(controller, 8),
			leftJoystickButton = new JoystickButton(controller , 9),
			rightJoystickButton = new JoystickButton(controller, 10);
	Manipulator manipulator = new Manipulator();
	DifferentialDrive driveTrain = new DifferentialDrive(new Talon(0), new Talon(1));
	AnalogInput angleSensor = new AnalogInput(0);
	AnalogInput infaredSensor = new AnalogInput(1);
	boolean autoRaiseToMiddle = false;
	DoubleSolenoid frontClimbPiston;
	DoubleSolenoid backClimbPiston;
	Compressor compressor;
	UsbCamera frontCamera;
	boolean intendToGoUp = false;
	boolean intendToGoDown = false;
	boolean wasWhite = false;
	double tabValue = 500;
	int solenoidInitCount = 0;

	boolean previousBackButton = false;
	boolean previousFrontButton = false;
	boolean nextDirectionIsForward = true;

	NetworkTableEntry elevatorPresetsToggle;
	NetworkTableEntry useAngleSensorToggle;

	/* Init functions are run ONCE when the robot is first started up and should be
	 * used for any initialization code. */
	public void robotInit() {
		if (compressor != null) {
			compressor.setClosedLoopControl(true);
		}
		CameraServer.getInstance().startAutomaticCapture();

		ShuffleboardLayout settingsList = Shuffleboard.getTab("Settings")
			.getLayout("List", "Settings");
		elevatorPresetsToggle = settingsList
			.add("Elevator Presets", false)
			.withWidget("Toggle Button")
			.getEntry();
		useAngleSensorToggle = settingsList
			.add("Use Angle Sensor", false)
			.withWidget("Toggle Button")
			.getEntry();
	}
	
	/* Periodic functions are ran several times a second the entire time the robot
	 * is enabled */
	public void robotPeriodic() {
	
	}

	public void autonomousInit() {
	}

	public void autonomousPeriodic() {
		teleopPeriodic();
	}

	public void teleopInit() {

	}

	public void teleopPeriodic() {
		intakeEjectPeriodic();
  		manipulatorAnglePeriodic();
		cameraPanTiltPeriodic();
		elevatorPeriodic();
		teleopDrivePeriodic();
		doubleSolenoidControl();
	}

	private void teleopDrivePeriodic() {

		double speed = 0.7;
		if (leftJoystickButton.get()){
			speed = 1;
		}
		driveTrain.arcadeDrive(
			speed * -controller.getRawAxis(1), 
			speed * controller.getRawAxis(0));
	}
	
	final int DPAD_UP = 0;
	final int DPAD_DOWN = 180;
	final int DPAD_RIGHT = 90;
	final int DPAD_LEFT = 270;

	private void intakeEjectPeriodic() {
		if(aButton.get() && bButton.get()){
			controller.setRumble(RumbleType.kLeftRumble, 1);
			controller.setRumble(RumbleType.kRightRumble, 1);
		} else {
			controller.setRumble(RumbleType.kLeftRumble, 0);
			controller.setRumble(RumbleType.kRightRumble, 0);
		}

		if(aButton.get() && manipulator.doesNotContainBall()) {
			manipulator.intake();
		}
		else if(bButton.get()) {
			manipulator.release();
		} else {
			manipulator.stopIntake();
		}
	}

	private void manipulatorAnglePeriodic() {

		boolean useAngleSensor = useAngleSensorToggle.getBoolean(false);

		if(rightJoystickButton.get()) {
			autoRaiseToMiddle = true;
		}

		double topAngleValue = 0.54;
		double bottomAngleValue = 0.72;
		double presetAngleValue = 0.436;
		double presetAngleRange = .02;
		double angleVoltage = angleSensor.getVoltage();

		if (yButton.get() && (!useAngleSensor || angleVoltage > topAngleValue)) {
			manipulator.angleRaise();
			autoRaiseToMiddle = false;
		} else if (xButton.get() && (!useAngleSensor || angleVoltage < bottomAngleValue)) {
			manipulator.angleLower();
			autoRaiseToMiddle = false;

		} else if(useAngleSensor && autoRaiseToMiddle && angleVoltage < (presetAngleValue - presetAngleRange / 2)) {
			manipulator.angleLower();
		} else if (useAngleSensor && autoRaiseToMiddle && angleVoltage > (presetAngleValue + presetAngleRange / 2)) { 
			manipulator.angleRaise();
		} else {
			manipulator.angleStop();
		}
	}

	private void cameraPanTiltPeriodic() {
		manipulator.cameraServoSide.set(controller.getRawAxis(5));
		manipulator.cameraServoUp.set(controller.getRawAxis(4));
	}

	private void elevatorManualPeriodic() {
		int dpadValue = controller.getPOV();
		if (dpadValue == DPAD_UP && !manipulator.elevatorTopLimit()){
			manipulator.elevatorRaise();
		} else if (dpadValue == DPAD_DOWN && !manipulator.elevatorBottomLimit()){
			manipulator.elevatorLower();
		} else{
			manipulator.elevatorStop();
		}
	}

	private void elevatorPresetLevelsPeriodic() {
		int dpadValue = controller.getPOV();
		
		if (dpadValue == DPAD_UP){
			intendToGoUp = true;
		}

		if (manipulator.elevatorTopLimit()){
			intendToGoUp = false;
		} else if (intendToGoUp) {
			manipulator.elevatorRaise();
		}
		
		if (dpadValue == DPAD_DOWN) {
			intendToGoDown = true;
		}

		if (manipulator.elevatorBottomLimit()){
			intendToGoDown = false;
		} else if (intendToGoDown) {
			manipulator.elevatorLower();
		}

		if (infaredSensor.getValue() < tabValue){
			wasWhite = true;
		}

		if (wasWhite && infaredSensor.getValue() >= tabValue){
			manipulator.elevatorStop();
			wasWhite = false;
			intendToGoUp = false;
			intendToGoDown = false;
		}
	}

	private void elevatorPeriodic() {
		
		boolean elevatorPresetsEnabled = elevatorPresetsToggle.getBoolean(false); 
		if (elevatorPresetsEnabled) {
			elevatorPresetLevelsPeriodic();
		} else {
			elevatorManualPeriodic();
		}
	}
	
	public void testPeriodic() {

	}

	public void doubleSolenoidControl() {
		if (frontClimbPiston == null || backClimbPiston == null) {
			return;
		}
		
		/*
		if (solenoidInitCount < 200	) { // lift solenoids for 4 seconds on startup
			frontClimbPiston.set(DoubleSolenoid.Value.kReverse);
			backClimbPiston.set(DoubleSolenoid.Value.kReverse);
			solenoidInitCount++;
			return;
		}
		*/
		
		if(!rightBumper.get()) {
			frontClimbPiston.set(DoubleSolenoid.Value.kOff);
		} else {
			if (previousFrontButton == false) {
				nextDirectionIsForward = !nextDirectionIsForward;
			}
			
			if (nextDirectionIsForward) {
				frontClimbPiston.set(DoubleSolenoid.Value.kForward);
			} else {
				frontClimbPiston.set(DoubleSolenoid.Value.kReverse);
			}
		}

		if(!leftBumper.get()) {
			backClimbPiston.set(DoubleSolenoid.Value.kOff);
		} else  {
			if (previousBackButton == false) {
				nextDirectionIsForward = !nextDirectionIsForward;
			}
			
			if (nextDirectionIsForward) {
				backClimbPiston.set(DoubleSolenoid.Value.kForward);
			} else {
				backClimbPiston.set(DoubleSolenoid.Value.kReverse);
			}
		}
		previousFrontButton = leftBumper.get();
		previousBackButton = rightBumper.get();
	}	
}
