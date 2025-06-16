package com.test.DockerStatusTest;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Session;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class Apollo2_iipsrv_Docker {

    @Test(priority = 1)
    public void containerStatus() {
        String vmIpAddress = "172.20.23.136";
        String sshUsername = "hbp";
        String sshPassword = "Health#123";
        String containerId = "6d933ac458e9";
        

        if (containerId.isEmpty()) {
            System.out.println("Container ID is required.");
            return;
        }

        try {
            // Connect to remote host using SSH
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(sshUsername, vmIpAddress, 22);
            sshSession.setPassword(sshPassword);
            sshSession.setConfig("StrictHostKeyChecking", "no");
            sshSession.connect();

            // Run docker inspect command
            ChannelExec channel = (ChannelExec) sshSession.openChannel("exec");
            channel.setCommand("docker inspect --format='{{.State.Status}}' " + containerId);
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));

            channel.connect();
            String line;
            boolean isRunning = false;
            while ((line = reader.readLine()) != null) {
                System.out.println("Docker Status: " + line.trim());
                if (line.trim().equals("running")) {
                    isRunning = true;
                }
            }

            channel.disconnect();
            sshSession.disconnect();

            // If not running, send alert
            if (!isRunning) {
                sendEmailAlert("Nathan bro 😩,\n\n"+"🚨 This is Apollo2 IIPSRV Docker. I am currently down. Kindly restart the container at your earliest convenience.");
                assert false : "Container is not in the expected state.";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEmailAlert(String messageBody) {
        String from = "automationsoftware25@gmail.com";
        String to = "nathan.i@htic.iitm.ac.in";
        String cc = "azizahammed.a@htic.iitm.ac.in,satheskumar@htic.iitm.ac.in";
        String subject = "Docker Container Alert - Apollo2 iipsrv";
        final String username = "automationsoftware25@gmail.com";
        final String password = "wjzcgaramsqvagxu";  

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(from, "Docker Monitor"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);
            System.out.println("✅ Alert email sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
