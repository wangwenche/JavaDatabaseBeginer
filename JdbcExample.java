package accessDatabase;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONObject;

public class JdbcExample {
	public static void main(String[] args) {
		try {
			// 读取保存URL、用户名和密码的JSON文件
			String content = new String(Files.readAllBytes(Paths.get("E:\\eclipse-workspace\\Lab02\\src\\accessDatabase\\configMsg.json")));
			JSONObject jsonObject = new JSONObject(content);
			String url = jsonObject.getString("db_url");
			String user = jsonObject.getString("db_username");
			String password = jsonObject.getString("db_password");
			
			// 加载MYSQL JDBC驱动
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			// 连接数据库
			Connection conn = DriverManager.getConnection(url, user, password);
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("There are four operations you can use to make changes to the database: ");
			System.out.println("Type \"view\" to view a record with a specified ID");
			System.out.println("Type \"insert\" to insert a new record");
			System.out.println("Type \"update\" to update the record for the specified ID");
			System.out.println("Type \"viewall\" to view all of the records");
			System.out.println("If you want to exit the program, type\"quit\" in any case");
			while(true) {
				System.out.println("Please enter the operation command(view, insert, update, quit, viewall)");
				String command = scanner.nextLine();
				
				switch(command.toLowerCase()) {
				case "view":
					viewData(conn, scanner);
					break;
				case "insert":
					insertData(conn, scanner);
					break;
				case "update":
					updateData(conn, scanner);
					break;
				case "viewall":
					viewAllData(conn);
					break;
				case "quit":
					return;
				default:
					System.out.println("Unknown command, please try again.");
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void viewData(Connection conn, Scanner scanner) {
		try {
			System.out.println("Please type the title of the book:");
			String inputData = scanner.nextLine();
			
			// 构建带参数的SQL查询
			String query = "SELECT * FROM book WHERE title LIKE ?";
			try(PreparedStatement pstmt = conn.prepareStatement(query)){
				pstmt.setString(1, "%" + inputData + "%");
				
				try(ResultSet rs = pstmt.executeQuery()){
					boolean hasData = false;
					while(rs.next()) {
						// 获取并打印每一列的数据
						int id = rs.getInt("id");
						String title = rs.getString("title");
						Date pubDate = rs.getDate("pubDate");
						String author = rs.getString("author");
						
						// 打印书籍信息
						System.out.println("ID: " + id + ", Title: " + title + ", Publication Date: " + pubDate + ", Author: " + author);
						hasData = true;
					}
					
					if(!hasData) {
						System.out.println("No matching books found.");
					}
				}
			}
		} catch (SQLException e) {
	        System.out.println("An error occurred while querying data:" + e.getMessage());
	    }
	}
	
	private static void insertData(Connection conn, Scanner scanner) {
	    try {
	        System.out.println("Please type the information about the data you want to insert into");
	        System.out.println("ID: ");
	        int id = scanner.nextInt();
	        scanner.nextLine(); // 消耗nextInt后的换行符

	        // 检查ID是否已存在
	        String checkQuery = "SELECT COUNT(*) FROM Book WHERE id = ?";
	        try(PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
	            checkStmt.setInt(1, id);
	            ResultSet rs = checkStmt.executeQuery();
	            if (rs.next() && rs.getInt(1) > 0) {
	                System.out.println("A book with ID " + id + " already exists. Please use a different ID.");
	                return; // 如果找到相同ID的书，则终止插入操作
	            }
	        }

	        System.out.println("Title: ");
	        String title = scanner.nextLine();
	        System.out.println("Publication Date (YYYY-MM-DD): ");
	        String pubDateStr = scanner.nextLine();
	        System.out.println("Author: ");
	        String author = scanner.nextLine();

	        // 将字符串日期转换为 java.sql.Date
	        java.sql.Date pubDate = java.sql.Date.valueOf(pubDateStr);

	        // 构建SQL插入语句
	        String query = "INSERT INTO Book (id, title, pubDate, author) VALUES (?, ?, ?, ?)";
	        try(PreparedStatement pstmt = conn.prepareStatement(query)){
	            pstmt.setInt(1,  id);
	            pstmt.setString(2, title);
	            pstmt.setDate(3, pubDate);
	            pstmt.setString(4, author);
	            
	            // 执行插入操作
	            int rowsAffected = pstmt.executeUpdate();
	            if(rowsAffected > 0) {
	                System.out.println(rowsAffected + " record(s) successfully inserted.");
	            } else {
	                System.out.println("No records inserted.");
	            }
	        }
	    } catch(SQLException e) {
	        System.out.println("An error occurred while inserting data: " + e.getMessage());
	    }
	}
	
	private static void updateData(Connection conn, Scanner scanner) {
	    try {
	        System.out.println("Please type the book's title whose information you want to update: ");
	        String title = scanner.nextLine();
	        System.out.println("Please type the field which you want to update(id, title, pubDate, author): ");
	        String field = scanner.nextLine().toLowerCase();

	        // 构造SQL语句
	        String query = "";
	        switch(field) {
	            case "id":
	                query = "UPDATE Book SET id = ? WHERE title = ?";
	                break;
	            case "title":
	                query = "UPDATE Book SET title = ? WHERE title = ?";
	                break;
	            case "pubdate":
	                query = "UPDATE Book SET pubDate = ? WHERE title = ?";
	                break;
	            case "author":
	                query = "UPDATE Book SET author = ? WHERE title = ?";
	                break;
	            default:
	                System.out.println("Unknown field, please try again.");
	                return; // 避免继续执行
	        }

	        try(PreparedStatement pstmt = conn.prepareStatement(query)){
	            // 根据不同的字段设置参数
	            if("id".equals(field)) {
	                System.out.println("Please type the new ID:");
	                int newId = Integer.parseInt(scanner.nextLine()); // 使用nextLine来避免换行符问题
	                pstmt.setInt(1, newId);
	            } else {
	                System.out.println("Please type the detailed data after updating:");
	                String data = scanner.nextLine();
	                if("pubdate".equals(field)) {
	                    pstmt.setDate(1, java.sql.Date.valueOf(data));
	                } else {
	                    pstmt.setString(1, data);
	                }
	            }
	            pstmt.setString(2, title);

	            // 执行更新操作
	            int rowsAffected = pstmt.executeUpdate();
	            if(rowsAffected > 0) {
	                System.out.println(rowsAffected + " record(s) successfully updated.");
	            } else {
	                System.out.println("No records updated.");
	            }
	        }
	    } catch(SQLException e) {
	        System.out.println("An error occurred while updating data: " + e.getMessage());
	    } catch(NumberFormatException e) {
	        System.out.println("Invalid number format: " + e.getMessage());
	    }
	}

	
	private static void viewAllData(Connection conn) {
		try {
			String query = "SELECT * FROM Book";
			try(PreparedStatement pstmt = conn.prepareStatement(query)){
				ResultSet rs = pstmt.executeQuery(query);
				while(rs.next()) {
					System.out.println(rs.getInt("id") + ", " + rs.getString("title") + ", " + rs.getDate("pubDate") + ", " + rs.getString("author"));
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
