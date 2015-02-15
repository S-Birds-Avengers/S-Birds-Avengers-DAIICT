/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 **To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import ab.Heuristic.HeuristicEngine;
import ab.Heuristic.TapTime;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.learn.VectorQuantizer;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class S_Birds_Avengers implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;


	File file = new File("AIBirds-Records.txt");

	// if file doesnt exists, then create it

	public FileWriter fw;
	public BufferedWriter bw;

	// a standalone implementation of the Naive Agent
	public S_Birds_Avengers() {

		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();

		try
		{
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		bw = new BufferedWriter(fw);
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		int cnt=1;
		while (true) {
			GameState state = solve();
			if (state == GameState.WON)
			{
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				int lastKey = 0;
				for(Integer key: scores.keySet()){

					lastKey=key;
					totalScore += scores.get(key);

					System.out.println(" Level " + key + " Score: " + scores.get(key) + " ");

				}
				String content=" Level " + lastKey + " Score: " + scores.get(lastKey) + " & Taken Attempts: " + cnt;

				try {
					bw.write(content);
					bw.newLine();
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println("Total Score: " + totalScore);
				System.out.println("Data Written Successfully\n");

				cnt=1;
				if(currentLevel>=21)
					break;
				aRobot.loadLevel(++currentLevel);

				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			}
			else if (state == GameState.LOST)
			{
				System.out.println("Restart");
				cnt++;
				if(cnt>15)
				{
					String content=" Level " + currentLevel + " Score: " + 0 + " & Taken Attempts: " + cnt;

					try {
						bw.write(content);
						bw.newLine();
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Data Written Successfully\n");
					cnt=1;

					aRobot.loadLevel(++currentLevel);
				}
				aRobot.restartLevel();
			}
			else if (state == GameState.LEVEL_SELECTION)
			{
				System.out
						.println("Unexpected level selection page, go to the last current level : "
								+ currentLevel);
				aRobot.loadLevel(currentLevel);
			}
			else if (state == GameState.MAIN_MENU)
			{
				System.out
						.println("Unexpected main menu page, go to the last current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
			else if (state == GameState.EPISODE_MENU)
			{
				System.out
						.println("Unexpected episode menu page, go to the last current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
		}
	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
					.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
		// get all the pigs
		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;
				{
					// random pick up a pig
					List<ABObject> allObjects = vision.getVisionRealShape().findObjects();
					List<ABObject> hill = vision.getVisionRealShape().findHills();
					HeuristicEngine heuristicEngine = new HeuristicEngine(sling,allObjects,pigs,hill,aRobot.getBirdTypeOnSling());
					TapTime time = new TapTime(sling, allObjects);
					List<ABObject> wood  = new LinkedList<ABObject>();
					List<ABObject> ice = new LinkedList<ABObject>();
					List<ABObject> stone = new LinkedList<ABObject>();
					List<ABObject> TNT = new LinkedList<ABObject>();
					ABObject blockToHit=null;
					wood.addAll(findWood(allObjects));
					ice.addAll(findIce(allObjects));
					stone.addAll(findStone(allObjects));
					TNT.addAll(findTNT(allObjects));
					VectorQuantizer vq = new VectorQuantizer(pigs,wood,ice,stone,TNT);
					Point refPoint = tp.getReferencePoint(sling);

					boolean check = true;
					Point targetPoint =null;

					ArrayList<ArrayList<ABObject>> checkRepeating = new ArrayList<ArrayList<ABObject>>();
					for(int i=0;i<21;i++)
						checkRepeating.add(new ArrayList<ABObject>());

					if (pigs.size() == 1) {
						System.out.println("!!!... Only 1 Pig ...!!!");
						blockToHit = pigs.get(0);
						System.out.println("Block to hit:\nType: " + blockToHit.getType() + ", Point: " + blockToHit.getCenter());

						List<Point> tempReleasePoints = tp.estimateLaunchPoint(sling, blockToHit.getCenter());
						List<Point> traj0 = tp.predictTrajectory(sling, tempReleasePoints.get(0));
						List<Point> traj1 = tp.predictTrajectory(sling, tempReleasePoints.get(1));

						boolean flag = true;
						for(Point trajPoint: traj0)
						{
							if(hill.contains(trajPoint) && trajPoint.getX()<blockToHit.getMinX())
							{
								flag = false;
								break;
							}
						}

						if(flag)
						{
							releasePoint = tempReleasePoints.get(0);
							check = false;
						}
						else
						{
							flag=true;
							for(Point trajPoint: traj1)
							{
								if(hill.contains(trajPoint) && trajPoint.getX()<blockToHit.getMinX())
								{
									flag = false;
									break;
								}
							}
							if(flag)
							{
								releasePoint = tempReleasePoints.get(1);
								check = false;
							}
						}
/*
							dx = (int) tempReleasePoints.get(0).getX() - refPoint.x;
							dy = (int) tempReleasePoints.get(0).getY() - refPoint.y;
							shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, 0);
							if (ABUtil.isReachable(vision, blockToHit.getCenter(), shot)) {

								releasePoint = tempReleasePoints.get(0);
								check = false;
							} else {
								if (tempReleasePoints.size() == 2) {
									releasePoint = tempReleasePoints.get(1);
									check = false;
								}
							}
*/
					}
					if (check)
					{
						System.out.println("!!!... More than 1 Pig ...!!!");
						Rectangle boundingRectangle = vq.getBoundingStructure();
						double[][][] qunantizedStructure = vq.quantize(boundingRectangle);

						heuristicEngine.generateAirBlocks(qunantizedStructure, boundingRectangle);
						//						System.out.println("Airblocks done!!");

						heuristicEngine.generateSubStructures();
						//						System.out.println("substructures done!!");

						heuristicEngine.calculateSupportWeight();
						//						System.out.println("Suuport done!!");
						heuristicEngine.downwardsFactor();
						heuristicEngine.addAir();
						heuristicEngine.getDisplacement();
						//						System.out.println("displcement done!!");
						heuristicEngine.penetrationWeight();
						System.out.println("All done!!");

						ArrayList<ABObject> finalCandidateBlocks = heuristicEngine.getFinalCandidateBlocks();

						boolean flag=true;
						while(flag)
						{
							blockToHit = heuristicEngine.blockAfterFinalFiltering(finalCandidateBlocks);
							if(checkRepeating.get(currentLevel-1).contains(blockToHit))
								finalCandidateBlocks.remove(blockToHit);
							else
							{
								checkRepeating.get(currentLevel-1).add(blockToHit);
								flag=false;
							}
						}
/*
						for(int i=0;i<finalCandidateBlocks.size();i++)
						{

							if(checkRepeating.get(currentLevel-1).contains(finalCandidateBlocks.get(i)))
							{
								flag = false;
								break;
							}
							else
							{
								blockToHit = finalCandidateBlocks.get(i);
								checkRepeating.get(currentLevel-1).add(blockToHit);
								break;
							}
						}
*/

						System.out.println("Block to hit:\nType: " + blockToHit.getType() + ", Point: " + new Point((int) blockToHit.getMinX(), (int) blockToHit.getCenterY()));
					}
					if(blockToHit.type!=ABType.Pig)
						targetPoint= new Point((int) blockToHit.getMinX(), (int) blockToHit.getCenterY());
					else
						targetPoint= blockToHit.getCenter();


//					ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, targetPoint);

//					System.out.println(pts.size());

					if(pts.isEmpty() || aRobot.getBirdTypeOnSling()==ABType.WhiteBird)
					{
						if(pts.isEmpty())
						{
							System.out.println("No release point found for the target");
							System.out.println("Try a shot with 45 degree");
						}
						releasePoint = tp.findReleasePoint(sling, Math.PI/4);
					}
					else if (releasePoint==null)
						releasePoint = pts.get(0);

					//Calculate the tapping time according the bird type
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println("Release Point: " + releasePoint);
						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						int tapInterval = 0;
						int tapTime = 0;
						switch (aRobot.getBirdTypeOnSling())
						{

							case RedBird:
								tapInterval = 0; break;               // start of trajectory
							case YellowBird:
								tapTime = (int) time.getYellowbirdTapTime(releasePoint);break; // 65-90% of the way
							case WhiteBird:
								tapInterval = (int) time.getWhitebirdTapTime(targetPoint);break; // 70-90% of the way
							case BlackBird:
								tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
							case BlueBird:
								tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
							default:
								tapInterval =  60;
						}

						if(aRobot.getBirdTypeOnSling()!=ABType.YellowBird)
						{
							tapTime = tp.getTapTime(sling, releasePoint, targetPoint, tapInterval);
						}
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
					{
						System.err.println("No Release Point Found");
						return state;
					}
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}


	public List<ABObject> findWood(List<ABObject> objects)
	{
		List<ABObject> ans = new LinkedList<ABObject>();
		for(ABObject obj:objects)
		{
			if(obj.type==ABType.Wood)
				ans.add(obj);
		}
		return ans;
	}

	public List<ABObject> findIce(List<ABObject> objects)
	{
		List<ABObject> ans = new LinkedList<ABObject>();
		for(ABObject obj:objects)
		{
			if(obj.type==ABType.Ice)
				ans.add(obj);
		}
		return ans;
	}

	public List<ABObject> findStone(List<ABObject> objects)
	{
		List<ABObject> ans = new LinkedList<ABObject>();
		for(ABObject obj:objects)
		{
			if(obj.type==ABType.Stone)
				ans.add(obj);
		}
		return ans;
	}

	public List<ABObject> findTNT(List<ABObject> objects)
	{
		List<ABObject> ans = new LinkedList<ABObject>();
		for(ABObject obj:objects)
		{
			if(obj.type==ABType.TNT)
				ans.add(obj);
		}
		return ans;
	}


	public static void main(String args[]) {

		S_Birds_Avengers na = new S_Birds_Avengers();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();
		System.out.println("☺ End ☺");
	}
}
