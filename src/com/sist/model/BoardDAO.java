package com.sist.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	String sql = null;

	/*
	 * 싱글톤 객체 만들기 1. 싱글톤 객체를 만들때는 우선적으로 접근지정자는 private로 선언한다. 2. 정적(static) 멤버로 선언을
	 * 한다.
	 */

	private static BoardDAO instance = new BoardDAO();

	/*
	 * 3. 기본생성자는 외부에서 접근이 되지 않도록 해야한다. 외부에서 new를 사용하지 못하게 하는 접근 기법
	 */

	private BoardDAO() {
	}

	// 4. 생성자 대신에 싱글톤 객체를 retrun 해 주는 getInstance() 메서드를 만들어 준다.
	public static BoardDAO getInstance() {
		if (instance == null) {
			instance = new BoardDAO();
		}
		return instance;
	}

	public void openConn() {

		try {
			// 1. JNDI객체 생성
			InitialContext ic = new InitialContext();

			// 2.lookup() 메서드를 이용하여 매칭되는 커넥션을 찾는다
			DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/myoracle");

			// 3. DataSource 객체를 이용하여 커넥션 객체를 하나 가져온다.
			con = ds.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // openConn() 메서드 end
		
	
	// board1 테이블의 전체 레코드를 조회하여 어레이리스트 객체에 저장

	public List<BoardDTO> getList() {
		List<BoardDTO> board = new ArrayList<BoardDTO>();

		try {
			openConn();
			sql = "select * from board1 order by board_no desc"; // 최신글을 맨 위에 보이게 하기 위해 글번호를 기준으로 내림차순 정렬
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) { // next() : 데이터가 존재하면 트루값 반환
				// 테이블에서 하나의 레코드를 가져와서 각각의 컬럼을 DTO 객체에 저장
				BoardDTO dto = new BoardDTO();
				dto.setBoard_no(rs.getInt("board_no"));
				// DTO의 Board_no에 set 해줘라. 무엇을? : DB의 board_no에서 가져온 int값을
				dto.setBoard_writer(rs.getString("board_writer"));
				dto.setBoard_title(rs.getString("board_title"));
				dto.setBoard_cont(rs.getString("board_cont"));
				dto.setBoard_pwd(rs.getString("board_pwd"));
				dto.setBoard_hit(rs.getInt("board_hit"));
				dto.setBoard_regdate(rs.getString("board_regdate"));
				board.add(dto); // board라는 리스트에 dto 주소값을 넣어준다

			}
			rs.close();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return board;
	} // getList()메서드의 end

	// board1 테이블의 글번호에 해당하는 조회수를 증가시키는 메서드
	public void boardHit(int no) {
		try {
			openConn();
			sql = "update board1 set board_hit = board_hit + 1 where board_no = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, no);
			pstmt.executeUpdate();

			// open된 객체 닫기
			pstmt.close();
			// con.close()는 하지 않는다 >> 글을 누르면 조회수만 증가하고 끝나는게 아니라 글의 내용을 봐야하므로 닫지 않는다.
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // boardHit() 메서드 end

	// 글 번호에 해당하는 글을 조회하는 메서드
	public BoardDTO getCont(int no) { // List가 아닌 DTO를 하는 이유 !! >> 모든 글이 아니라 해당하는 글 하나의 내용만 가져올 것이므로!

		BoardDTO dto = new BoardDTO();

		try {
			openConn();
			sql = "select * from board1 where board_no = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, no);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				dto.setBoard_no(rs.getInt("board_no"));
				// DTO의 Board_no에 set 해줘라. 무엇을? : DB의 board_no에서 가져온 int값을
				dto.setBoard_writer(rs.getString("board_writer"));
				dto.setBoard_title(rs.getString("board_title"));
				dto.setBoard_cont(rs.getString("board_cont"));
				dto.setBoard_pwd(rs.getString("board_pwd"));
				dto.setBoard_hit(rs.getInt("board_hit"));
				dto.setBoard_regdate(rs.getString("board_regdate"));
			}

			// open 객체 닫기
			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dto;
	} // getCont() 메서드의 end

	// BOARD1 테이블에 게시글을 추가하는 메서드
	public int insertBoard(BoardDTO dto) {
		int result = 0;

		try {
			openConn();
			sql = "insert into board1 values(board1_seq.nextval, ?, ?, ?, ?, default, sysdate)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, dto.getBoard_writer());
			pstmt.setString(2, dto.getBoard_title());
			pstmt.setString(3, dto.getBoard_cont());
			pstmt.setString(4, dto.getBoard_pwd());
			result = pstmt.executeUpdate();

			// open 객체 닫기
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	// BOARD1 테이블의 게시글을 수정하는 메서드
	public int updateBoard(BoardDTO dto) {

		int result = 0;

		try {
			openConn();
			sql = "select * from board1 where board_no =?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, dto.getBoard_no());
			rs = pstmt.executeQuery();

			if (rs.next()) {
				if (dto.getBoard_pwd().equals(rs.getString("board_pwd"))) {
					// dto.getBoard_pwd = 수정창에서 입력한 비밀번호, rs.getString("board_pwd") = DB에 있는 비밀번호
					sql = "update board1 set board_title = ?, board_cont=? where board_no=?";
					pstmt = con.prepareStatement(sql);
					pstmt.setString(1, dto.getBoard_title());
					pstmt.setString(2, dto.getBoard_cont());
					pstmt.setInt(3, dto.getBoard_no());
					result = pstmt.executeUpdate(); // 성공시 1값 반환
				} else { // 비밀번호가 틀린 경우
					result = -1;
				}
			}

			// open 객체 닫기
			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	} // updateBoard() 메서드의 end

	// board1 테이블에서 게시글을 삭제하는 메서드

	public int deleteBoard(int no, String pwd) {

		int result = 0;

		try {
			openConn();
			sql = "select * from board1 where board_no=?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, no);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				if (pwd.equals(rs.getString("board_pwd"))) {
					// dto.getBoard_pwd = 수정창에서 입력한 비밀번호, rs.getString("board_pwd") = DB에 있는 비밀번호
					sql = "delete from board1 where board_no=?";
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, no);
					result = pstmt.executeUpdate(); // 성공시 1값 반환
				} else { // 비밀번호가 틀린 경우
					result = -1;
				}
			}

			// open 객체 닫기
			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	} // deleteBoard 메서드의 end

	// board1 테이블에서 게시물을 검색하는 메서드
	public List<BoardDTO> searchBoard(String find_field, String find_name) {

		List<BoardDTO> search = new ArrayList<BoardDTO>();

		// 제목으로 검색한 경우
		try {
			openConn();
			int a = 0;
			if (find_field.equals("title")) {
				sql = "select * from board1 where board_title like ?";
				a = 1;
			} else if (find_field.equals("cont")) {
				sql = "select * from board1 where board_cont like ?";
				a = 1;
			} else {
				sql = "select * from board1 where board_title like ? or board_cont like ?";
				a = 2;
			}

			pstmt = con.prepareStatement(sql);
			if (a == 1) {
				pstmt.setString(1, "%" + find_name + "%"); // like 검색할때 %검색어%
			} else if (a == 2) {
				pstmt.setString(1, "%" + find_name + "%");
				pstmt.setString(2, "%" + find_name + "%");
			}
			rs = pstmt.executeQuery();

			while (rs.next()) { // next() : 데이터가 존재하면 트루값 반환
				// 테이블에서 하나의 레코드를 가져와서 각각의 컬럼을 DTO 객체에 저장
				BoardDTO dto = new BoardDTO();
				dto.setBoard_no(rs.getInt("board_no"));
				// DTO의 Board_no에 set 해줘라. 무엇을? : DB의 board_no에서 가져온 int값을
				dto.setBoard_writer(rs.getString("board_writer"));
				dto.setBoard_title(rs.getString("board_title"));
				dto.setBoard_cont(rs.getString("board_cont"));
				dto.setBoard_pwd(rs.getString("board_pwd"));
				dto.setBoard_hit(rs.getInt("board_hit"));
				dto.setBoard_regdate(rs.getString("board_regdate"));
				search.add(dto); // board라는 리스트에 dto 주소값을 넣어준다

			}
			rs.close();
			pstmt.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return search;
	} // searchBoard 메서드의 end

}