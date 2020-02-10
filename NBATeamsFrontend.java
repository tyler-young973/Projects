import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 * Starter code gotten from DBDemo
 */
public class NBATeams {
	private final Scanner sc = new Scanner(System.in);

	private String userName = "";

	private String password = "";

	private final String serverName = "localhost";

	/** The port of the MySQL server (default is 3306) */
	private final int portNumber = 3306;

	private final String dbName = "nbateam";
	
	private Connection conn = null;
	
	/**
	 * Get a new database connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);

		conn = DriverManager.getConnection("jdbc:mysql://"
				+ this.serverName + ":" + this.portNumber + "/" + this.dbName,
				connectionProps);

		return conn;
	}

	public void insert() {
		System.out.println("Which table would you like to insert to?");
		System.out.println("1. Players\n2. Teams\n3. Coaches\n4. Games\n5. Stats");
		String cmd = sc.nextLine();
		try{
	           Integer.parseInt(cmd);
	    } catch (NumberFormatException ex){
	           cmd = cmd.toLowerCase();
	    }
		while (!cmd.equals("1") &&
				!cmd.equals("players") &&
				!cmd.equals("2") &&
				!cmd.equals("teams") &&
				!cmd.equals("3") &&
				!cmd.equals("coaches") &&
				!cmd.equals("4") &&
				!cmd.equals("games") &&
				!cmd.equals("5") &&
				!cmd.equals("stats")) {
			System.out.println("Invalid command. Please enter one of the available commands above.");
			cmd = sc.nextLine();
			if (cmd.equals("")) {
				System.out.println("Insertion cancelled.");
				return;
			}
			try{
				Integer.parseInt(cmd);
		    } catch (NumberFormatException ex){
		    	cmd = cmd.toLowerCase();
		    }
		}
		if (cmd.equals("1") || cmd.equals("players")) {
			String playerFirstName = "", playerLastName = "", teamName = "", jerseyNo = "", position = "";
			while (true) {
				System.out.println("Enter the player's jersey number: ");
				jerseyNo = sc.nextLine();
				try {
					Integer.parseInt(jerseyNo);
				} catch (NumberFormatException e) {
					System.out.println("Jersey Number has to be an integer.");
					continue;
				}
				try {
					PreparedStatement pst = conn.prepareStatement("SELECT has_jersey_no(?) AS jersey_exists");
					pst.setString(1, jerseyNo);
					ResultSet rs = pst.executeQuery();
					rs.next();
		            System.out.println(rs.getString("jersey_exists"));
		            if (rs.getString("jersey_exists").equals("1")) {
		            	System.out.println(jerseyNo + " number already exists.");
		            } else {
		            	break;
		            }
				} catch (SQLException e) {
					System.out.println("Error.");
					return;
				}
			}
			System.out.println("Enter the player's first name: ");
			playerFirstName = sc.nextLine();
			System.out.println("Enter the player's last name: ");
			playerLastName = sc.nextLine();
			System.out.println("Enter the player's position: ");
			System.out.println("Available positions:\nF: Forward\nG: Guard\nC: Center");
			position = sc.nextLine().toLowerCase();
			while (!position.equals("f") && !position.equals("forward") &&
					!position.equals("g") && !position.equals("guard") &&
			        !position.equals("c") && !position.equals("center")) {
				System.out.println("Invalid position entered.");
				System.out.println("Available positions:\nF: Forward\nG: Guard\nC: Center");
				position = sc.nextLine().toLowerCase();
			}
			if (position.equals("f") || position.equals("forward")) {
				position = "F";
			} else if (position.equals("g") || position.equals("guard")) {
				position = "G";
			} else if (position.equals("c") || position.equals("center")) {
				position = "C";
			}
			while (true) {
				System.out.println("Enter the player's team name: ");
				teamName = sc.nextLine();
				if (teamName.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				try {
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, teamName);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("0")) {
		            	System.out.println("Team does not exist.");
		            } else {
		            	break;
		            }
				} catch (SQLException e) {
					System.out.println("Invalid input");
				}
			}
			try { 
				CallableStatement insertPlayer = conn.prepareCall("CALL insert_player(?, ?, ?, ?, ?)");
				insertPlayer.setString(1, jerseyNo);
				insertPlayer.setString(2, position);
				insertPlayer.setString(3, playerFirstName);
				insertPlayer.setString(4, playerLastName);
				insertPlayer.setString(5, teamName);
				insertPlayer.execute();
				System.out.println("Player successfully inserted.");
			} catch (SQLException e) {
				System.out.println("Failed to enter player into the database. Please try again later.");
				return;
			}
			
		} else if (cmd.equals("2") || cmd.equals("teams")) {
			String teamName, conference;
			System.out.println("Please enter the team name to insert:");
			teamName = sc.nextLine();
			System.out.println("Please enter the conference of " + teamName + ".");
			System.out.println("Available conferences are\nE: Eastern\nW: Western");
			conference = sc.nextLine().toLowerCase();
			while (!conference.equals("e") &&
					!conference.equals("eastern") &&
					!conference.equals("w") &&
					!conference.equals("western")) {
				System.out.println("Please enter a valid conference.");
				conference = sc.nextLine().toLowerCase();
				if (conference.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
			}
			conference = conference.toLowerCase();
			if (conference.equals("e") || conference.equals("eastern")) {
				conference = "Eastern";
			} else if (conference.equals("w") || conference.equals("western")) {
				conference = "Western";
			}
			try { 
				CallableStatement insertTeam = conn.prepareCall("CALL insert_team(?, ?)");
				insertTeam.setString(1, teamName);
				insertTeam.setString(2, conference);
				insertTeam.execute();
				System.out.println("Team successfully inserted.");
			} catch (SQLException e) {
				System.out.println("Failed to enter team into the database. Please try again later.");
				return;
			}
		} else if (cmd.equals("3") || cmd.equals("coaches")) {
			String coachFirstName, coachLastName, teamName, isActive;
			System.out.println("Enter the coach's first name: ");
			coachFirstName = sc.nextLine();
			System.out.println("Enter the coach's last name: ");
			coachLastName = sc.nextLine();
			while (true) {
				System.out.println("Enter the coach's team name: ");
				teamName = sc.nextLine();
				if (teamName.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				try {
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, teamName);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("0")) {
		            	System.out.println("Team does not exist.");
		            } else {
		            	break;
		            }
				} catch (SQLException e) {
					System.out.println("Invalid input");
				}
			}
			System.out.println("Is the coach still active?\nY: Yes\nN: No");
			isActive = sc.nextLine().toLowerCase();
			while (!isActive.equals("y") && !isActive.equals("yes")
					&& !isActive.equals("n") && !isActive.equals("no")) {
				System.out.println("Invalid answer. Please answer either Y: Yes or N: No");
				isActive = sc.nextLine().toLowerCase();
				if (isActive.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
			}
			isActive = isActive.toLowerCase();
			if (isActive.equals("y") || isActive.equals("yes")) {
				isActive = "1";
			} else if (isActive.equals("n") || isActive.equals("no")) {
				isActive = "0";
			}
			try { 
				CallableStatement insertCoach = conn.prepareCall("CALL insert_coach(?, ?, ?, ?)");
				insertCoach.setString(1, coachFirstName);
				insertCoach.setString(2, coachLastName);
				insertCoach.setString(3, teamName);
				insertCoach.setString(4, isActive);
				insertCoach.execute();
				System.out.println("Coach successfully inserted.");
			} catch (SQLException e) {
				System.out.println("Failed to enter coach into the database. Please try again later.");
				return;
			}
		} else if (cmd.equals("4") || cmd.equals("games")) {
			String opponentTeam, isHome, homeScore, awayScore, date, dayStr, monthStr, yearStr;
			String[] dayMonthYear;
			while (true) {
				System.out.println("Enter the opponent's team name: ");
				opponentTeam = sc.nextLine();
				if (opponentTeam.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				try {
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, opponentTeam);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("0")) {
		            	System.out.println("Team does not exist.");
		            } else {
		            	break;
		            }
				} catch (SQLException e) {
					System.out.println("Invalid input");
				}
			}
			System.out.println("Is the game a home game?\nY: Yes\nN: No");
			isHome = sc.nextLine().toLowerCase();
			while (!isHome.equals("y") && !isHome.equals("yes")
					&& !isHome.equals("n") && !isHome.equals("no")) {
				System.out.println("Invalid answer. Please answer either Y: Yes or N: No");
				isHome = sc.nextLine().toLowerCase();
			}
			if (isHome.equals("y") || isHome.equals("yes")) {
				isHome = "1";
			} else if (isHome.equals("n") || isHome.equals("no")) {
				isHome = "0";
			}
			while (true) {
				System.out.println("Enter the home score.");
				homeScore = sc.nextLine();
				if (homeScore.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				try {
					Integer.parseInt(homeScore);
					break;
				} catch (NumberFormatException e) {
					System.out.println("Home score has to be an integer.");
					continue;
				}
			}
			
			while (true) {
				System.out.println("Enter the away score.");
				awayScore = sc.nextLine();
				if (awayScore.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				try {
					Integer.parseInt(awayScore);
					break;
				} catch (NumberFormatException e) {
					System.out.println("Away score has to be an integer.");
					continue;
				}
			}
			// day month year
			System.out.println("Please enter the day, month, and year (seperated by spaces) of the game.");
			date = sc.nextLine();
			dayMonthYear = date.split(" ");
			while (true) {
				if (date.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				if (dayMonthYear.length != 3) {
					System.out.println("Invalid format.");
				} else {
					dayStr = dayMonthYear[0];
					monthStr = dayMonthYear[1];
					yearStr = dayMonthYear[2];
					try {
						Integer.parseInt(dayStr);
						Integer.parseInt(monthStr);
						Integer.parseInt(yearStr);
						break;
					} catch (NumberFormatException e) {
						System.out.println("Invalid format.");
					}
				}
				System.out.println("Please enter the day, month, and year (seperated by spaces) of the game.");
				date = sc.nextLine();
			}
			date = yearStr + "-" + monthStr + "-" + dayStr;
			try { 
				PreparedStatement insertGame = conn.prepareStatement("CALL insert_game(?, ?, ?, ?, ?)");
				insertGame.setString(1, opponentTeam);
				insertGame.setString(2, homeScore);
				insertGame.setString(3, awayScore);
				insertGame.setString(4, isHome);
				insertGame.setString(5, date);
				insertGame.execute();
				System.out.println("Game successfully inserted.");
			} catch (SQLException e) {
				System.out.println("Failed to enter game into the database. Please try again later.");
				e.printStackTrace();
				return;
			}
		} else if (cmd.equals("5") || cmd.equals("stats")) {
			System.out.println("Enter the player's jersey number");
			String jerseyNumber = sc.nextLine();
			try {
				Integer.parseInt(jerseyNumber);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Update cancelled.");
				return;
			}
			while (true) {
				try {
					PreparedStatement pst = conn.prepareStatement("SELECT has_jersey_no(?) AS jersey_exists");
					pst.setString(1, jerseyNumber);
					ResultSet rs = pst.executeQuery();
					rs.next();
			        System.out.println(rs.getString("jersey_exists"));
			        if (rs.getString("jersey_exists").equals("0")) {
			            System.out.println(jerseyNumber + " number does not exist. Enter another jersey number.");
			            jerseyNumber = sc.nextLine();
			        } else {
			        	break;
			        }
				} catch (SQLException e) {
					System.out.println("Error.");
					return;
				}
			}
			System.out.println("What is the game number?");
			String gameNo = sc.nextLine();
			try {
				Integer.parseInt(gameNo);
			} catch (NumberFormatException e) {
				System.out.println("Invalid game number, update is now cancelled.");
				return;
			}
			while (true) {
				try {
					// not done
					PreparedStatement pst = conn.prepareStatement("SELECT has_game_no(?) AS valid_game_no");
					pst.setString(1, gameNo);
					ResultSet gameNoValid = pst.executeQuery();
					gameNoValid.next();
					if (gameNoValid.getString("valid_game_no").equals("0")) {
						System.out.println("Game number does not exist. Enter another game number.");
						gameNo = sc.nextLine();
					} else {
						break;
					}
				} catch (SQLException e) {
					System.out.println("Error has occurred. Insert is now cancelled.");
					return;
				}
			}
			System.out.println("Enter the minutes played.");
			String minutes = sc.nextLine();
			try {
				Integer.parseInt(minutes);
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer. Insertion is now cancelled.");
				return;
			}
			System.out.println("Enter the points scored.");
			String points = sc.nextLine();
			try {
				Integer.parseInt(points);
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer. Insertion is now cancelled.");
				return;
			}
			System.out.println("Enter the number rebounds.");
			String rebounds = sc.nextLine();
			try {
				Integer.parseInt(rebounds);
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer. Insertion is now cancelled.");
				return;
			}
			System.out.println("Enter the number of assists.");
			String assists = sc.nextLine();
			try {
				Integer.parseInt(assists);
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer. Insertion is now cancelled.");
				return;
			}
			try {
				PreparedStatement insertStats = conn.prepareStatement(
						"INSERT INTO player_stats (jersey_number, game_no, minutes, points, rebounds, assists)"
						+ "VALUES (?, ?, ?, ?, ?, ?)");
				insertStats.setString(1, jerseyNumber);
				insertStats.setString(2, gameNo);
				insertStats.setString(3, minutes);
				insertStats.setString(4, points);
				insertStats.setString(5, rebounds);
				insertStats.setString(6, assists);
				insertStats.execute();
			} catch (SQLException e) {
				System.out.println("Failed to insert. May be caused by duplicate primary keys. Insert cancelled.");
				return;
			}
		}
	}
	
	public void view() {
		System.out.println("What would you like to view?");
		System.out.println("1. Players\n2. Teams\n3. Coaches\n4. Games\n5. Stats");
		String cmd = sc.nextLine();
		try{
	           Integer.parseInt(cmd);
	    } catch (NumberFormatException ex){
	           cmd = cmd.toLowerCase();
	    }
		while (!cmd.equals("1") &&
				!cmd.equals("players") &&
				!cmd.equals("2") &&
				!cmd.equals("teams") &&
				!cmd.equals("3") &&
				!cmd.equals("coaches") &&
				!cmd.equals("4") &&
				!cmd.equals("games") &&
				!cmd.equals("5") &&
				!cmd.equals("stats")) {
			System.out.println("Invalid command. Please enter one of the available commands above.");
			if (cmd.equals("")) {
				System.out.println("Insertion cancelled.");
				return;
			}
			cmd = sc.nextLine();
			try{
				Integer.parseInt(cmd);
		    } catch (NumberFormatException ex){
		    	cmd = cmd.toLowerCase();
		    }
		}
		if (cmd.equals("1") || cmd.equals("players")) {
			String inputSrch;
			System.out.println("Search players by jersey number, first name, last name, position (F, C, G), or team.");
			inputSrch = sc.nextLine();
			PreparedStatement pst = null;
			ResultSet rs;
			try {
				try {
					Integer.parseInt(inputSrch);
					pst = conn.prepareStatement("CALL search_player_by_jersey(?)");
				    pst.setString(1, inputSrch);
				} catch (NumberFormatException e) {
					if (pst != null) pst.close();
					if (inputSrch.equals("F") || inputSrch.equals("C") || inputSrch.equals("G")) {
						pst = conn.prepareStatement("CALL search_player_by_position(?)");
						pst.setString(1, inputSrch);
					} else {
						if (pst != null) pst.close();
						pst = conn.prepareStatement("CALL search_player_by_name_team(?)");
						pst.setString(1, inputSrch);
					}
				}
				rs = pst.executeQuery();
				System.out.println(String.format("%-9s | %-8s | %-20s | %-30s | %-30s",
						"jersey no", "position", "first name", "last name", "team name"));
				for (int i = 0; i < 95; i++) {
			    	System.out.print("-");
			    }
			    System.out.println();
			    while(rs.next()) {
			    	System.out.println(String.format("%-9s | %-8s | %-20s | %-30s | %-30s",
			    			rs.getString("jersey_number"),
			    			rs.getString("position"),
			    			rs.getString("player_first_name"),
			    			rs.getString("player_last_name"),
			    			rs.getString("team_name")));
			    }
			    rs.close();
			    pst.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				System.out.println("Error searching the database. ");
			}
			
		} else if (cmd.equals("2") || cmd.equals("teams")) {
			System.out.println("1. Search team by name\n2. Eastern conference teams\n3. Western conference teams");
			String in = sc.nextLine();
			while (!in.equals("1") &&
					!in.equals("2") &&
					!in.equals("3")) {
				System.out.println("Invalid input. Please enter 1, 2, or 3.");
				if (in.equals("")) {
					System.out.println("Insertion cancelled.");
					return;
				}
				in = sc.nextLine();
			}
			PreparedStatement pst = null;
			ResultSet rs = null;
			try {
				if (in.equals("1")) {
					System.out.println("Enter team name.");
					String teamName = sc.nextLine();
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, teamName);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("0")) {
			            System.out.println("Team does not exist.");
			            return;
			        }
					pst = conn.prepareStatement("CALL search_team_by_name(?)");
					pst.setString(1, teamName);
				} else {
					String conference = "";
					if (in.equals("2")) {
						conference = "eastern";
					} else if (in.contentEquals("3")) {
						conference = "western";
					}
					pst = conn.prepareStatement("CALL list_team_conference(?)");
					pst.setString(1, conference);
				}
				rs = pst.executeQuery();
				System.out.println(String.format("%-7s | %-20s | %-10s",
		    			"team no",
		    			"team name",
		    			"conference"));
				for (int i = 0; i < 50; i++) {
			    	System.out.print("-");
			    }
				System.out.println("");
				while(rs.next()) {
			    	System.out.println(String.format("%-7s | %-20s | %-10s",
			    			rs.getString("team_no"),
			    			rs.getString("team_name"),
			    			rs.getString("conference")));
			    }
			    rs.close();
			    pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (cmd.equals("3") || cmd.equals("coaches")) {
			System.out.println("Search coach by first name, last name, or team name.");
			String input = sc.nextLine();
			try {
				PreparedStatement pst = conn.prepareStatement("CALL search_coach(?)");
				pst.setString(1, input);
				ResultSet rs = pst.executeQuery();
				rs = pst.executeQuery();
				System.out.println(String.format("%-8s | %-20s | %-20s | %-6s | %-20s",
		    			"coach no",
		    			"first name",
		    			"last name",
		    			"active",
		    			"team"));
				for (int i = 0; i < 75; i++) {
			    	System.out.print("-");
			    }
				System.out.println("");
				while(rs.next()) {
			    	System.out.println(String.format("%-8s | %-20s | %-20s | %-6s | %-20s",
			    			rs.getString("coach_no"),
			    			rs.getString("coach_first_name"),
			    			rs.getString("coach_last_name"),
			    			rs.getString("is_active"),
			    			rs.getString("team_name")));
			    }
			    rs.close();
			    pst.close();
			} catch(SQLException e) {
				System.out.println("Error viewing coaches. Please try again later");
			}
			
		} else if (cmd.equals("4") || cmd.equals("games")) {
			System.out.println("Search game by team name.");
			String name = sc.nextLine();
			try {
				PreparedStatement pst = conn.prepareStatement("CALL search_game(?)");
				pst.setString(1, name);
				ResultSet rs = pst.executeQuery();
				rs = pst.executeQuery();
				System.out.println(String.format("%-20s | %-7s | %-4s | %-4s | %-10s",
		    			"opponent",
		    			"is home",
		    			"home",
		    			"away",
		    			"date"));
				for (int i = 0; i < 50; i++) {
			    	System.out.print("-");
			    }
				System.out.println("");
				while(rs.next()) {
			    	System.out.println(String.format("%-20s | %-7s | %-4s | %-4s | %-10s",
			    			rs.getString("team_name"),
			    			rs.getString("home_boolean"),
			    			rs.getString("home_score"),
			    			rs.getString("away_score"),
			    			rs.getString("game_date")));
			    }
			    rs.close();
			    pst.close();
			} catch(SQLException e) {
				System.out.println("Error viewing game. Please try again later");
			}
		} else if (cmd.equals("5") || cmd.equals("stats")) {
			System.out.println("Enter a player's jersey number.");
			String in = sc.nextLine();
			try {
				PreparedStatement pst = conn.prepareStatement("SELECT * FROM player_stats WHERE jersey_number = (?)");
				pst.setString(1, in);
				ResultSet rs = pst.executeQuery();
				rs = pst.executeQuery();
				System.out.println(String.format("%-6s | %-4s | %-4s | %-3s | %-8s | %-7s",
		    			"jersey",
		    			"game",
		    			"mins",
		    			"pts",
		    			"rebounds",
		    			"assists"));
				for (int i = 0; i < 45; i++) {
			    	System.out.print("-");
			    }
				System.out.println("");
				while(rs.next()) {
			    	System.out.println(String.format("%-6s | %-4s | %-4s | %-3s | %-8s | %-7s",
			    			rs.getString("jersey_number"),
			    			rs.getString("game_no"),
			    			rs.getString("minutes"),
			    			rs.getString("points"),
			    			rs.getString("rebounds"),
			    			rs.getString("assists")));
			    }
			    rs.close();
			    pst.close();
			} catch(SQLException e) {
				System.out.println("Error with given jersey number.");
			}
		} 
	}
	
	public void update() {
		String table, whichTuple, field, content, input;
		System.out.println("Which table would you like to update?");
		System.out.println("1. Players\n2. Teams\n3. Coaches\n4. Games\n5. Stats");
		String cmd = sc.nextLine();
		try{
	           Integer.parseInt(cmd);
	    } catch (NumberFormatException ex){
	           cmd = cmd.toLowerCase();
	    }
		while (!cmd.equals("1") &&
				!cmd.equals("players") &&
				!cmd.equals("2") &&
				!cmd.equals("teams") &&
				!cmd.equals("3") &&
				!cmd.equals("coaches") &&
				!cmd.equals("4") &&
				!cmd.equals("games") &&
				!cmd.equals("5") &&
				!cmd.equals("stats")) {
			System.out.println("Invalid command. Please enter one of the available commands above.");
			cmd = sc.nextLine();
			try{
				Integer.parseInt(cmd);
		    } catch (NumberFormatException ex){
		    	cmd = cmd.toLowerCase();
		    }
		}
		if (cmd.equals("1") || cmd.equals("players")) {
			System.out.println("Whose information would you like to update? Enter the player's jersey number");
			input = sc.nextLine();
			System.out.println("What would you like to update? (Please enter choice 1, 2, or 3)");
			System.out.println("1. First name\n2. Last name\n3. Team");
			field = sc.nextLine();
			while (!field.equals("1") && !field.equals("2") && !field.equals("3")) {
				System.out.println("Invalid input. Please enter 1, 2, or 3.");
				field = sc.nextLine();
			}
			System.out.println("What would you like to change it to? (Press Enter if you want to cancel.)");
			content = sc.nextLine();
			if (content.equals("")) return;
			try {
				PreparedStatement pst = null;
				if (field.equals("3")) {
					pst = conn.prepareStatement("CALL update_player_team(?, ?)");
					pst.setString(1, input);
					pst.setString(2, content);
					pst.execute();
				} else if (field.equals("1")) {
					pst = conn.prepareStatement("UPDATE player SET player_first_name = ? WHERE jersey_number = ?");
					pst.setString(1, content);
					pst.setString(2, input);
					pst.executeUpdate();
				} else if (field.equals("2")) {
					pst = conn.prepareStatement("UPDATE player SET player_last_name = ? WHERE jersey_number = ?");
					pst.setString(1, content);
					pst.setString(2, input);
					pst.executeUpdate();
				}
				System.out.println("Player successfully updated.");
			} catch (SQLException e) {
				System.out.println("Unable to update. Please try again later.");
			}
			
		} else if (cmd.equals("2") || cmd.equals("teams")) {
			System.out.println("Which team would you like to update?");
			String teamName = sc.nextLine();
			try {
				PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
				checkTeamExists.setString(1, teamName);
				ResultSet teamExists = checkTeamExists.executeQuery();
				teamExists.next();
				if (teamExists.getString("valid_team").equals("0")) {
	            	System.out.println("Team does not exist.");
	            	return;
				}
			} catch (SQLException e) {
				System.out.println("Invalid input");
			}
			System.out.println("What would you like to update?");
			System.out.println("1. Team name\n2. Conference");
			String choice = sc.nextLine();
			if (!choice.equals("1") && !choice.equals("2")) {
				System.out.println("Invalid choice.");
				return;
			}
			if (choice.equals("1")) {
				System.out.println("What is the new name?");
				String newName = sc.nextLine();
				try {
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, newName);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("1")) {
		            	System.out.println("Team already exists.");
		            	return;
					}
					PreparedStatement pst = conn.prepareStatement("CALL update_team_name(?, ?)");
					pst.setString(1, teamName);
					pst.setString(2, newName);
					pst.executeQuery();
				} catch (SQLException e) {
					System.out.println("Unable to update. Please try again later.");
				}
			} else if (choice.equals("2")) {
				System.out.println("Conference E: Eastern W: Western");
				String conference = sc.nextLine();
				while (!conference.equals("e") &&
						!conference.equals("eastern") &&
						!conference.equals("w") &&
						!conference.equals("western")) {
					System.out.println("Invalid input. E: Eastern W: Western");
					conference = sc.nextLine().toLowerCase();
				}
				if (conference.equals("e") || conference.equals("eastern")) {
					conference = "Eastern";
				} else if (conference.equals("w") || conference.equals("western")) {
					conference = "Western";
				}
				try {
					PreparedStatement pst = conn.prepareStatement("CALL update_team_conference(?, ?)");
					pst.setString(1, teamName);
					pst.setString(2, conference);
					pst.executeQuery();
				} catch (SQLException e) {
					System.out.println("Unable to update. Please try again later.");
				}
			}
		} else if (cmd.equals("3") || cmd.equals("coaches")) {
			System.out.println("Who would you like to update? Please enter the coach's first name and last name seperated by spaces.");
			String fullName = sc.nextLine();
			String[] coachFullName = fullName.split("\\s+");
			
			if (coachFullName.length != 2) {
				System.out.println("Invalid input.");
				return;
			}
			try {
				// not done
				PreparedStatement pst = conn.prepareStatement("SELECT has_coach(?, ?) AS valid_coach");
				pst.setString(1, coachFullName[0]);
				pst.setString(2, coachFullName[1]);
				ResultSet rs = pst.executeQuery();
				rs.next();
				
				if (rs.getString("valid_coach").equals("0")) {
					System.out.println("Coach does not exist.");
					return;
				}
				System.out.println("What would you like to update? (Choose 1, 2, 3, or 4 for choices)");
				System.out.println("1. First Name\n2. Last Name\n3. Team\n4. Active Status");
				String choice = sc.nextLine();
				while (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
					System.out.println("Invalid choice.");
					return;
				}
				if (choice.equals("1")) {
					System.out.println("What would you like to change the first name to? (Press Enter to cancel)");
					String newName = sc.nextLine();
					if (newName.equals("")) {
						System.out.println("Cancelled name update.");
						return;
					}
					PreparedStatement updateCoach = conn.prepareStatement(
							"UPDATE head_coach SET coach_first_name = ? WHERE coach_first_name = ? AND coach_last_name = ?");
					updateCoach.setString(1, newName);
					updateCoach.setString(2, coachFullName[0]);
					updateCoach.setString(3, coachFullName[1]);
					updateCoach.executeQuery();
					
				} else if (choice.equals("2")) {
					System.out.println("What would you like to change the last name to? (Press Enter to cancel)");
					String newName = sc.nextLine();
					if (newName.equals("")) {
						System.out.println("Cancelled name update.");
						return;
					}
					PreparedStatement updateCoach = conn.prepareStatement(
							"UPDATE head_coach SET coach_last_name = ? WHERE coach_first_name = ? AND coach_last_name = ?");
					updateCoach.setString(1, newName);
					updateCoach.setString(2, coachFullName[0]);
					updateCoach.setString(3, coachFullName[1]);
					updateCoach.executeUpdate();
				} else if (choice.equals("3")) {
					System.out.println("What would you like to change the team to? (Press Enter to cancel)");
					String newTeam = sc.nextLine();
					if (newTeam.equals("")) {
						System.out.println("Update cancelled.");
					}
					PreparedStatement checkTeamExists = conn.prepareStatement("SELECT has_team_name(?) AS valid_team");
					checkTeamExists.setString(1, newTeam);
					ResultSet teamExists = checkTeamExists.executeQuery();
					teamExists.next();
					if (teamExists.getString("valid_team").equals("0")) {
		            	System.out.println("Team does not exist.");
		            	return;
					}
					// not done
					PreparedStatement updateCoach = conn.prepareStatement("CALL update_coach_team(?, ?, ?)");
					updateCoach.setString(1, newTeam);
					updateCoach.setString(2, coachFullName[0]);
					updateCoach.setString(3, coachFullName[1]);
					updateCoach.executeQuery();
				} else if (choice.equals("4")) {
					System.out.println("Enter 1 to change coaches status to active. Enter 0 for not active. Enter any other keys to cancel update.");
					String newActive = sc.nextLine();
					if (!newActive.equals("1") && !newActive.equals("0")) {
						System.out.println("Update cancelled.");
						return;
					}
					PreparedStatement updateCoach = conn.prepareStatement(
							"UPDATE coach SET is_active = ? WHERE coach_first_name = ? AND coach_last_name = ?");
					updateCoach.setString(1, newActive);
					updateCoach.setString(2, coachFullName[0]);
					updateCoach.setString(3, coachFullName[1]);
					updateCoach.executeQuery();
				}
			} catch (SQLException e) {
				System.out.println("Unable to update. Please try again later.");
			}
		} else if (cmd.equals("4") || cmd.equals("games")) {
			System.out.println("What game number with would you like to update? Enter any other key to cancel.");
			String gameNo = sc.nextLine();
			try {
				Integer.parseInt(gameNo);
			} catch (NumberFormatException e) {
				System.out.println("Invalid game number, update is now cancelled.");
				return;
			}
			try {
				// not done
				PreparedStatement pst = conn.prepareStatement("SELECT has_game_no(?) AS valid_game_no");
				pst.setString(1, gameNo);
				ResultSet gameNoValid = pst.executeQuery();
				gameNoValid.next();
				if (gameNoValid.getString("valid_game_no").equals("0")) {
					System.out.println("Game number does not exist.");
					return;
				}
			} catch (SQLException e) {
				System.out.println("Error has occurred. Update is now cancelled.");
			}
			System.out.println("What would you like too update?");
			System.out.println("1. Opponent Team\n2. Home boolean\n3. Home Score\n4. Away score\n5. Game date");
			String nextCmd = sc.nextLine();
			if (!nextCmd.equals("1") && !nextCmd.equals("2") && !nextCmd.equals("3") &&
					!nextCmd.equals("4") && !nextCmd.equals("5")) {
				System.out.println("Invalid input. Update cancelled.");
				return;
			}
			try {
				// none
				PreparedStatement pst = null;
				if (!nextCmd.equals("1")) {
					System.out.println("Enter the new opponent team's name.");
					String newOpponentName = sc.nextLine();
					pst = conn.prepareStatement("CALL update_game_opponent(?, ?)");
					pst.setString(1, gameNo);
					pst.setString(2, newOpponentName);
				} else if (!nextCmd.equals("2")) {
					System.out.println("Enter the new home boolean. Either 0 or 1");
					String homeBoolean = sc.nextLine();
					if (!homeBoolean.equals("0") && !homeBoolean.equals("1")) {
						System.out.println("Invalid boolean. Update is now cancelled.");
						return;
					}
					pst = conn.prepareStatement(
							"UPDATE game SET home_boolean = ? WHERE game_no = ?");
					pst.setString(1, homeBoolean);
					pst.setString(2, gameNo);
					pst.executeUpdate();
				} else if (!nextCmd.equals("3")) {
					System.out.println("Enter the new home score.");
					String homeScore = sc.nextLine();
					try {
						Integer.parseInt(homeScore);
					} catch (NumberFormatException e) {
						System.out.println("Invalid home score. Update is now cancelled.");
					}
					pst = conn.prepareStatement("UPDATE game SET home_score = ? WHERE game_no = ?");
					pst.setString(1, homeScore);
					pst.setString(2, gameNo);
				} else if (!nextCmd.equals("4")) {
					System.out.println("Enter the new home score.");
					String awayScore = sc.nextLine();
					try {
						Integer.parseInt(awayScore);
					} catch (NumberFormatException e) {
						System.out.println("Invalid away score. Update is now cancelled.");
					}
					pst = conn.prepareStatement("UPDATE game SET away_score = ? WHERE game_no = ?");
					pst.setString(1, awayScore);
					pst.setString(2, gameNo);
					pst.executeUpdate();
				} else if (!nextCmd.equals("5")) {
					String dayStr, monthStr, yearStr;
					System.out.println("Please enter the day, month, and year (seperated by spaces) of the game.");
					String date = sc.nextLine();
					String[] dayMonthYear = date.split(" ");
					while (true) {
						if (dayMonthYear.length != 3) {
							System.out.println("Invalid format.");
						} else {
							dayStr = dayMonthYear[0];
							monthStr = dayMonthYear[1];
							yearStr = dayMonthYear[2];
							try {
								Integer.parseInt(dayStr);
								Integer.parseInt(monthStr);
								Integer.parseInt(yearStr);
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid format.");
							}
						}
						System.out.println("Please enter the day, month, and year (seperated by spaces) of the game.");
						date = sc.nextLine();
					}
					date = monthStr + "-" + dayStr + "-" + yearStr;
					pst = conn.prepareStatement("UPDATE game SET game_date = ? WHERE game_no = ?");
					pst.setString(1, date);
					pst.setString(2, gameNo);
					pst.executeUpdate();
				}
			} catch (SQLException e) {
				System.out.println("Update failed. Please try again later.");
				return;
			}
			
		} else if (cmd.equals("5") || cmd.equals("stats")) {
			System.out.println("Which player stats would you like to update?");
			System.out.println("Choose player by 1. jersey number or 2. First name and last name");
			String choice = sc.nextLine();
			if (!choice.equals("1") && !choice.equals("2")) {
				System.out.println("Invalid choice. Update cancelled.");
				return;
			}
			String jerseyNumber = "";
			if (choice.equals("1")) {
				System.out.println("Enter the player's jersey number.");
				jerseyNumber = sc.nextLine();
				try {
					Integer.parseInt(jerseyNumber);
				} catch (NumberFormatException e) {
					System.out.println("Invalid input. Update cancelled.");
					return;
				}
				try {
					PreparedStatement pst = conn.prepareStatement("SELECT has_jersey_no(?) AS jersey_exists");
					pst.setString(1, jerseyNumber);
					ResultSet rs = pst.executeQuery();
					rs.next();
		            System.out.println(rs.getString("jersey_exists"));
		            if (rs.getString("jersey_exists").equals("0")) {
		            	System.out.println(jerseyNumber + " number does not exist.");
		            	return;
		            }
				} catch (SQLException e) {
					System.out.println("Error.");
					return;
				}
				
			} else if (choice.equals("2")) {
				System.out.println("Enter the player's first name.");
				String firstName = sc.nextLine();
				System.out.println("Enter the player's last name.");
				String lastName = sc.nextLine();
				try {
					PreparedStatement findJerseyNo = conn.prepareStatement("SELECT find_jersey_no_from_name(?, ?) AS jerseyNoOut");
					findJerseyNo.setString(1, firstName);
					findJerseyNo.setString(2, lastName);
					ResultSet rs = findJerseyNo.executeQuery();
					rs.next();
					jerseyNumber = rs.getString("jerseyNoOut");
				} catch (SQLException e) {
					System.out.println("Error.");
					return;
				}
			}
			System.out.println("What is the game number?");
			String gameNo = sc.nextLine();
			try {
				Integer.parseInt(gameNo);
			} catch (NumberFormatException e) {
				System.out.println("Invalid game number, update is now cancelled.");
				return;
			}
			try {
				PreparedStatement pst = conn.prepareStatement("SELECT has_game_no(?) AS valid_game_no");
				pst.setString(1, gameNo);
				ResultSet gameNoValid = pst.executeQuery();
				gameNoValid.next();
				if (gameNoValid.getString("valid_game_no").equals("0")) {
					System.out.println("Game number does not exist.");
					return;
				}
			} catch (SQLException e) {
				System.out.println("Error has occurred. Update is now cancelled.");
				return;
			}
			System.out.println("Which field would you like to update?");
			System.out.println("1. minutes\n2. points\n3. rebounds\n4. assists");
			String fieldToUpdate = sc.nextLine();
			if (fieldToUpdate.equals("1")) {
				fieldToUpdate = "minutes";
			} else if (fieldToUpdate.equals("2")) {
				fieldToUpdate = "points";
			} else if (fieldToUpdate.equals("3")) {
				fieldToUpdate = "rebounds";
			} else if (fieldToUpdate.equals("4")) {
				fieldToUpdate = "assists";
			} else {
				System.out.println("Invalid choice. Update is now cancelled.");
				return;
			}
			System.out.println("What is the new value? Enter any other key to cancel.");
			String newValue = sc.nextLine();
			try {
				Integer.parseInt(newValue);
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer. Update cancelled.");
			}
			try {
				PreparedStatement pst = conn.prepareStatement(
						"UPDATE player_stats SET ? = ?  WHERE jersey_no = ? AND game_no = ?");
				pst.setString(1, fieldToUpdate);
				pst.setString(2, newValue);
				pst.setString(3, jerseyNumber);
				pst.setString(4, gameNo);
				pst.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Invalid input. Update has been cancelled");
				return;
			}
		}
	}
	
	public void delete() {
		System.out.println("Which table would you like to delete to?");
		System.out.println("1. Players\n2. Teams\n3. Coaches\n4. Games\n5. Stats");
		String cmd = sc.nextLine();
		try{
	           Integer.parseInt(cmd);
	    } catch (NumberFormatException ex){
	           cmd = cmd.toLowerCase();
	    }
		while (!cmd.equals("1") &&
				!cmd.equals("players") &&
				!cmd.equals("2") &&
				!cmd.equals("teams") &&
				!cmd.equals("3") &&
				!cmd.equals("coaches") &&
				!cmd.equals("4") &&
				!cmd.equals("games") &&
				!cmd.equals("5") &&
				!cmd.equals("stats")) {
			System.out.println("Invalid command. Please enter one of the available commands above.");
			cmd = sc.nextLine();
			try{
				Integer.parseInt(cmd);
		    } catch (NumberFormatException ex){
		    	cmd = cmd.toLowerCase();
		    }
		}
		if (cmd.equals("1") || cmd.equals("players")) {
			System.out.println("Delete player by jersey number.");
			String num = sc.nextLine();
			try { 
				// TODO
				CallableStatement deletePlayer = conn.prepareCall("CALL delete_player(?)");
				deletePlayer.setString(1, num);
				deletePlayer.execute();
				System.out.println("Player successfully deleted.");
			} catch (SQLException e) {
				System.out.println("Failed to delete player. Please try again later.");
			}
		} else if (cmd.equals("2") || cmd.equals("teams")) {
			System.out.println("Delete team by name");
			String name = sc.nextLine();
			try { 
				// TODO
				CallableStatement deleteTeam = conn.prepareCall("CALL delete_team(?)");
				deleteTeam.setString(1, name);
				deleteTeam.execute();
				System.out.println("Team successfully deleted.");
			} catch (SQLException e) {
				System.out.println("Failed to delete team. Please try again later.");
			}
		} else if (cmd.equals("3") || cmd.equals("coaches")) {
			System.out.println("Delete coaches by last name and team");
			System.out.println("Enter last name:");
			String lastName = sc.nextLine();
			System.out.println("Enter team name:");
			String teamName = sc.nextLine();
			try { 
				// TODO
				CallableStatement deleteCoach = conn.prepareCall("CALL delete_coach(?, ?)");
				deleteCoach .setString(1, lastName);
				deleteCoach .setString(2, teamName);
				deleteCoach .execute();
				System.out.println("Coach successfully deleted.");
			} catch (SQLException e) {
				System.out.println("Failed to delete coach. Please try again later.");
			}
		} else if (cmd.equals("4") || cmd.equals("games")) {
			System.out.println("Delete game by game number.");
			String gameNo = sc.nextLine();
			try { 
				// TODO
				CallableStatement deleteGame = conn.prepareCall("CALL delete_game(?)");
				deleteGame .setString(1, gameNo);
				System.out.println("Game successfully deleted.");
			} catch (SQLException e) {
				System.out.println("Failed to delete game. Please try again later.");
			}
		} else if (cmd.equals("5") || cmd.equals("stats")) {
			System.out.println("Delete stats by jersey number and game number");
			System.out.println("Enter jersey number:");
			String jerseyNumber = sc.nextLine();
			System.out.println("Enter game number:");
			String gameNumber = sc.nextLine();
			try { 
				// TODO
				CallableStatement deleteStats = conn.prepareCall("CALL delete_stats(?, ?)");
				deleteStats.setString(1, jerseyNumber);
				deleteStats.setString(2, gameNumber);
				deleteStats.execute();
				System.out.println("Stats successfully deleted.");
			} catch (SQLException e) {
				System.out.println("Failed to delete stats. Please try again later.");
			}
		}
	}
	
	public void loop() {
		while (true) {
			System.out.println("Please enter a command: ");
			System.out.println("Available commands:\n1. Insert\n2. View\n3. Update\n4. Delete\n5. Exit");
			String cmd = sc.nextLine();
			try{
		           Integer.parseInt(cmd);
		    } catch (NumberFormatException ex){
		           cmd = cmd.toLowerCase();
		    }
			while (!cmd.equals("1") &&
					!cmd.equals("insert") &&
					!cmd.equals("2") &&
					!cmd.equals("view") &&
					!cmd.equals("3") &&
					!cmd.equals("update") &&
					!cmd.equals("4") &&
					!cmd.equals("delete") &&
					!cmd.equals("5") &&
					!cmd.equals("exit")) {
				System.out.println("Invalid command. Please enter one of the available commands above.");
				cmd = sc.nextLine();
				try{
			           Integer.parseInt(cmd);
			    } catch (NumberFormatException ex){
			           cmd = cmd.toLowerCase();
			    }
			}
			if (cmd.equals("5") || cmd.equals("exit")) {
				System.out.println("Application will now terminate.");
				break;
			}
			if (cmd.equals("1") || cmd.equals("insert")) {
				this.insert();
			}
			if (cmd.equals("2") || cmd.equals("view")) {
				this.view();
			}
			if (cmd.equals("3") || cmd.equals("update")) {
				this.update();
			}
			if (cmd.equals("4") || cmd.equals("delete")) {
				this.delete();
			}
		}
	}
	
	/**
	 * Connect to MySQL and do some stuff.
	 * @throws SQLException 
	 */
	public void run() throws SQLException {
		
		try {
			System.out.println("username: ");
			this.userName = sc.nextLine();
			System.out.println("password: ");
			this.password = sc.nextLine();
			conn = this.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to enter the database.");
			return;
		}
		conn = this.getConnection();
		this.loop();
	}
	
	/**
	 * Connect to the DB and do some stuff
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		NBATeams app = new NBATeams();
		app.run();
	}
}
