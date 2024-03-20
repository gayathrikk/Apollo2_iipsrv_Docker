package com.test.DockerStatusTest;

import com.jcraft.jsch.*;

import org.testng.annotations.Test;

import java.io.BufferedReader;

import java.io.InputStreamReader;

public class Apollo2_iipsrv_Docker {
	
	@Test(priority=1)

	public void containerStatus() {

		String vmIpAddress = "172.16.20.232";

        String username = "hbp";

        String password = "Health#123"; 

        String containerId = "216d7ffbed10"; 
	System.out.println("apollo2 iipsrv docker id = "+containerId);



        if (containerId.isEmpty()) {

            System.out.println("Container ID is required.");

            return;

        }



        try {

            JSch jsch = new JSch();

            Session session = jsch.getSession(username, vmIpAddress, 22);

            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no"); 

            session.connect();



            // Execute the docker inspect command to check the container's status

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand("docker inspect --format='{{.State.Status}}' " + containerId);

            channel.connect();



            // Read the output of the command

            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println(line);

                assert line.equals("running") : "Container is not in the expected state.";

            }



            channel.disconnect();

            session.disconnect();

        } catch (Exception e) {

            e.printStackTrace();

        }

		

	}

}
