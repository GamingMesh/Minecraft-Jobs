package me.alex.jobs.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import me.alex.jobs.Job;
import me.alex.jobs.Jobs;

import org.bukkit.entity.Player;

/*
 * username:experience:level:job
 */
public class FlatFileJobsDAO implements JobsDAO{
	private Jobs plugin = null;
	private String fileLocation = null;
	
	public FlatFileJobsDAO(String fileLocation, Jobs plugin) {
		this.plugin = plugin;
		this.fileLocation = fileLocation;
	}
	
	@Override
	public Job findPlayer(Player player) {
		Job job = null;
		try{
			File inFile = new File(fileLocation);
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				String strLine;
				while(in.hasNextLine()){
					strLine = in.nextLine();
					String[] jobInfo = strLine.split(":");
					if(jobInfo[0].equals(player.getName())){
						job = new Job(jobInfo[3], Integer.parseInt(jobInfo[1]), Integer.parseInt(jobInfo[2]), plugin, player);
					}
				}
				in.close();
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return job;
	}
	
	@Override
	public void changeJob(Player player, String newJob) {
		try{
			File inFile = new File(fileLocation);
			if(!inFile.exists()){
				try{
					inFile.createNewFile();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
			Scanner in = new Scanner(inFile);
			String strLine;
			ArrayList<String> flatFile = new ArrayList<String>();
			boolean savedFile = false;
			while(in.hasNextLine()){
				strLine = in.nextLine();
				String[] jobInfo = strLine.split(":");
				if(jobInfo[0].equals(player.getName())){
					// found
					savedFile = true;
					flatFile.add(player.getName() + ":0:1:" + newJob);
				}
				else{
					flatFile.add(strLine);
				}
			}
			if(!savedFile){
				flatFile.add(player.getName() + ":0:1:" + newJob);
			}
			in.close();
			
			//write out
			PrintWriter out = new PrintWriter(new FileWriter(fileLocation));
			for(String temp: flatFile){
				out.println(temp);
			}
			out.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void saveJob(Player player, Job job) {
		try{
			Scanner in = new Scanner(new File(fileLocation));
			String strLine;
			ArrayList<String> flatFile = new ArrayList<String>();
			boolean savedFile = false;
			while(in.hasNextLine()){
				strLine = in.nextLine();
				String[] jobInfo = strLine.split(":");
				if(jobInfo[0].equals(player.getName())){
					// found
					savedFile = true;
					flatFile.add(player.getName() + ":" + job.getExperience() + ":" + job.getLevel() + ":" + job.getJobName());
				}
				else{
					flatFile.add(strLine);
				}
			}
			if(!savedFile){
				flatFile.add(player.getName() + ":" + job.getExperience() + ":" + job.getLevel() + ":" + job.getJobName());
			}
			in.close();
			
			//write out
			PrintWriter out = new PrintWriter(new FileWriter(fileLocation));
			for(String temp: flatFile){
				out.println(temp);
			}
			out.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
