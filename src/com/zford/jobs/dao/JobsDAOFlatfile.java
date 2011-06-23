package com.zford.jobs.dao;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import org.bukkit.entity.Player;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.PlayerJobInfo;
import com.zford.jobs.dao.container.JobsDAOData;

public class JobsDAOFlatfile implements JobsDAO {
	private String saveLocation = "plugins/Jobs/jobs.data";

	@Override
	public List<JobsDAOData> getAllJobs(Player player) {
		ArrayList<JobsDAOData> list = new ArrayList<JobsDAOData>();
		try{
			File inFile = new File(saveLocation);
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				String strLine;
				while(in.hasNextLine()){
					strLine = in.nextLine();
					String[] jobInfo = strLine.split(":");
					if(jobInfo.length == 4 && jobInfo[0].equalsIgnoreCase(player.getName())){
						list.add(new JobsDAOData(jobInfo[3], Integer.parseInt(jobInfo[1]), Integer.parseInt(jobInfo[2])));
					}
				}
				in.close();
			}
		}
		catch (Exception e){
			System.err.println("[Jobs] - Error loading jobs.data. Disabling plugin!");
			e.printStackTrace();
			Jobs.disablePlugin();
		}
		return list;
	}

	@Override
	public void joinJob(Player player, Job job) {
		try{
			// read file in
			File inFile = new File(saveLocation);
			ArrayList<String> file = new ArrayList<String>();
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				while(in.hasNextLine()){
					file.add(in.nextLine());
				}
				in.close();
			}
			
			// write file out
			PrintWriter out = new PrintWriter(new FileWriter(saveLocation));
			for(String temp: file){
				out.println(temp);
			}
			// add the new job
			out.println(player.getName()+":0:1:"+job.getName());
			out.close();
		}
		catch (Exception e){
			System.err.println("[Jobs] - Error loading jobs.data. Disabling plugin!");
			e.printStackTrace();
			Jobs.disablePlugin();
		}
	}

	@Override
	public void quitJob(Player player, Job job) {
		try{
			// read file in
			File inFile = new File(saveLocation);
			ArrayList<String> file = new ArrayList<String>();
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				String strLine;
				while(in.hasNextLine()){
					strLine = in.nextLine();
					String[] jobInfo = strLine.split(":");
					if(jobInfo.length == 4 && !jobInfo[0].equalsIgnoreCase(player.getName())
							&& !jobInfo[3].equalsIgnoreCase(job.getName())){
						file.add(strLine);
					}
				}
				in.close();
			}
			
			// write file out
			PrintWriter out = new PrintWriter(new FileWriter(saveLocation));
			for(String temp: file){
				out.println(temp);
			}
			out.close();
		}
		catch (Exception e){
			System.err.println("[Jobs] - Error loading jobs.data. Disabling plugin!");
			Jobs.disablePlugin();
		}
	}

	@Override
	public void save(PlayerJobInfo jobInfo) {
		try{
			// read file in
			File inFile = new File(saveLocation);
			ArrayList<String> file = new ArrayList<String>();
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				String strLine;
				while(in.hasNextLine()){
					strLine = in.nextLine();
					String[] jobData = strLine.split(":");
					if(jobData.length == 4 && !jobData[0].equalsIgnoreCase(jobInfo.getPlayer().getName())){
						file.add(strLine);
					}
				}
				in.close();
			}
			
			// write file out
			PrintWriter out = new PrintWriter(new FileWriter(saveLocation));
			for(String temp: file){
				out.println(temp);
			}
			for(JobProgression prog: jobInfo.getJobsProgression()){
				out.println(jobInfo.getPlayer().getName() + ":" + (int)prog.getExperience() + 
						":" + prog.getLevel() + ":" + prog.getJob().getName());
			}
			out.close();
		}
		catch (Exception e){
			System.err.println("[Jobs] - Error loading jobs.data. Disabling plugin!");
			e.printStackTrace();
			Jobs.disablePlugin();
		}
	}

	@Override
	public Integer getSlotsTaken(Job job) {
		Integer slots = 0;
		try{
			File inFile = new File(saveLocation);
			if(inFile.exists()){
				Scanner in = new Scanner(inFile);
				String strLine;
				while(in.hasNextLine()){
					strLine = in.nextLine();
					String[] jobInfo = strLine.split(":");
					if(jobInfo.length == 4 && jobInfo[3].equalsIgnoreCase(job.getName())){
						++slots;
					}
				}
				in.close();
			}
		}
		catch (Exception e){
			System.err.println("[Jobs] - Error loading jobs.data. Disabling plugin!");
			e.printStackTrace();
			Jobs.disablePlugin();
		}
		return slots;
	}

}
