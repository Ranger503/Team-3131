/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.*;

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
			Front = new DoubleSolenoid(1, 0);
			Back = new DoubleSolenoid(3, 2);
			c = new Compressor(0);
		} catch (Exception e) {
			System.out.print("Cannot initialize all pneumatics!!!!!!!!!!!!!!!!!!!!");
			System.out.print(e.toString());
		}
	}

	Joystick controller = new Joystick(0);
	Button button1 = new JoystickButton(controller, 1),
			button2 = new JoystickButton(controller, 2),
			button3 = new JoystickButton(controller, 3),
			button4 = new JoystickButton(controller, 4),
			button5 = new JoystickButton(controller, 5),
			button6 = new JoystickButton(controller, 6),
			button7 = new JoystickButton(controller, 7),
			button8 = new JoystickButton(controller, 8);
	Manipulator manipulator = new Manipulator();
	DifferentialDrive driveTrain = new DifferentialDrive(new Talon(0), new Talon(1));
	
	
	DoubleSolenoid Front;
	DoubleSolenoid Back;
	Compressor c;

	/* Init functions are run ONCE when the robot is first started up and should be
	 * used for any initialization code. */
	public void robotInit() {
		if (c != null) {
			c.setClosedLoopControl(true);
		}
		CameraServer.getInstance().startAutomaticCapture();
	}
	
	/* Periodic functions are ran several times a second the entire time the robot
	 * is enabled */
	public void robotPeriodic() {
		
	}

	public void autonomousInit() {
		
	}

	public void autonomousPeriodic() {

	}

	public void teleopInit() {
		
	}
	public void teleopPeriodic() {
		teleopManipulatorPeriodic();
		teleopDrivePeriodic();
		doubleSolenoidControl();
		
	}
	private void teleopDrivePeriodic() {
		driveTrain.arcadeDrive(-controller.getRawAxis(1), controller.getRawAxis(0));
		
	}

	private void teleopManipulatorPeriodic() {
		if(button1.get() && button2.get()){
			//controller.setRumble(, 1);
			
		}
		if(button1.get() && !manipulator.containsBall()){
			manipulator.intake();
			
		}
		else if(button2.get()) {
			manipulator.release();
			
		} else {
			manipulator.stopIntake();
		}
		
		
		if (button3.get()) {
			manipulator.raise();
			
		}
		else if (button4.get()) {
			manipulator.lower();
		
		}
		else{
			manipulator.stopRaise();
		//I'm... OUT!
			
		}
	}

	public void testPeriodic() {

	}
	public void doubleSolenoidControl() {
		if (Front == null || Back == null) {
			return;
		}
		if(button5.get()) {
			Front.set(DoubleSolenoid.Value.kForward);
		}
		else { 
			Front.set(DoubleSolenoid.Value.kReverse);
		}
		if(button6.get()) {
			Back.set(DoubleSolenoid.Value.kForward);
		}
		else {
			Back.set(DoubleSolenoid.Value.kReverse);
		}
		
	}
		
}
