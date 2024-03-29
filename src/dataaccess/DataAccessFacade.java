package dataaccess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import business.AccessLevel;
import business.Book;
import business.CheckoutRecord;
import business.Member;
import business.User;

public class DataAccessFacade implements DataAccess {
	public enum DbType {
		BOOKS, MEMBERS, USERS, CHECKOUTRECORD
	}

	public static final String OUTPUT_DIR = System.getProperty("user.dir") + "//src//dataaccess//storage";
	public static final String DATE_PATTERN = "MM/dd/yyyy";
	private static HashMap<String, Book> books;
	private static HashMap<String, CheckoutRecord> checkoutRecords;
	private static HashMap<String, User> users;
	private static HashMap<String, Member> members;
	private int defaulId;

	public DataAccessFacade() {
		getBooks();
		getUsers();
		getMembers();
		getCheckoutRecords();
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Book> getBooks() {
		if (books == null) {
			books = (HashMap<String, Book>) readFromStorage(DbType.BOOKS);
		}
		return books;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, User> getUsers() {
		if (users == null) {
			users = (HashMap<String, User>) readFromStorage(DbType.USERS);
		}
		return users;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Member> getMembers() {
		if (members == null) {
			members = (HashMap<String, Member>) readFromStorage(DbType.MEMBERS);
		}
		defaulId = members == null ? 0 : members.size();
		return members;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, CheckoutRecord> getCheckoutRecords() {
		if (checkoutRecords == null) {
			checkoutRecords = (HashMap<String, CheckoutRecord>) readFromStorage(DbType.CHECKOUTRECORD);
		}
		return checkoutRecords;
	}

	static void writeToStorage(DbType type, Object ob) {
		ObjectOutputStream out = null;
		try {
			if (type == DbType.MEMBERS) {
				HashMap<String, Member> map = (members != null) ? (HashMap<String, Member>) members
						: new HashMap<String, Member>();
				Member member = (Member) ob;
				map.put(member.getMemberId(), member);
				ob = map;
				members = map;
			} else if (type == DbType.BOOKS) {
				HashMap<String, Book> map = (books != null) ? (HashMap<String, Book>) books
						: new HashMap<String, Book>();
				Book book = (Book) ob;
				map.put(book.getId(), book);
				ob = map;
				books = map;
			} else if (type == DbType.USERS) {
				HashMap<String, User> map = (users != null) ? (HashMap<String, User>) users
						: new HashMap<String, User>();
				User user = (User) ob;
				map.put(user.getUsername(), user);
				ob = map;
				users = map;
			} else if (type == DbType.CHECKOUTRECORD) {
				HashMap<String, CheckoutRecord> map = (checkoutRecords != null)
						? (HashMap<String, CheckoutRecord>) checkoutRecords
						: new HashMap<String, CheckoutRecord>();
				CheckoutRecord record = (CheckoutRecord) ob;
				map.put(record.getRecordId(), record);
				ob = map;
				checkoutRecords = map;
			}

			Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
			out = new ObjectOutputStream(Files.newOutputStream(path));
			out.writeObject(ob);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	static Object readFromStorage(DbType type) {
		ObjectInputStream in = null;
		Object retVal = null;
		try {
			Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
			in = new ObjectInputStream(Files.newInputStream(path));
			retVal = in.readObject();
		} catch (Exception e) {
			System.out.println("creating new file " + type.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return retVal;
	}

	// book
	@Override
	public void saveNewBook(Book book) {
		writeToStorage(DbType.BOOKS, book);
	}

	@Override
	public HashMap<String, Book> readBooksMap() {
		return getBooks();
	}

	public List<Book> readBookList() {
		HashMap<String, Book> books = getBooks();
		List<Book> bookList = new ArrayList<Book>();
		for (Book entry : books.values()) {
			bookList.add(entry);
		}
		return bookList;
	}

	@Override
	public boolean addCopy(String isbn, int number) {
		Book book = searchBook(isbn);
		if (book != null) {
			book.setNumberOfCopy(book.getNumberOfCopy() + number);
			book.setTotalNumOfCopy(book.getTotalNumOfCopy() + number);
			saveNewBook(book);
			return true;
		}
		return false;
	}

	@Override
	public Book searchBook(String isbn) {
		HashMap<String, Book> books = getBooks();
		for (Book book : books.values()) {
			if (book.getIsbn().equals(isbn)) {
				return book;
			}
		}
		return null;
	}

	// user
	@Override
	public HashMap<String, User> readUserMap() {
		return getUsers();
	}

	@Override
	public void saveUserMap(User user) {
		writeToStorage(DbType.USERS, user);
	}

	@Override
	public AccessLevel userLogin(String username, String password) {
		HashMap<String, User> users = readUserMap();
		User user = users.get(username);
		if (user != null && user.getPassword().equals(password)) {
			return user.getAccessLevel();
		}
		return AccessLevel.NONE;
	}

	// member
	@Override
	public HashMap<String, Member> readMemberMap() {
		return getMembers();
	}

	@Override
	public List<Member> readMember() {
		HashMap<String, Member> members = getMembers();
		List<Member> membersList = new ArrayList<Member>();
		for (Member entry : members.values()) {
			membersList.add(entry);
		}
		return membersList;
	}

	@Override
	public void saveNewMember(Member member) {
		if (member.getMemberId() == null)
			member.setMemberId(String.format("1%06d", defaulId));
		writeToStorage(DbType.MEMBERS, member);
		getMembers();
	}

	@Override
	public boolean updateMembers(Member m, String id) {
		HashMap<String, Member> updateMap = getMembers();
		if (updateMap != null) {
			if (updateMap.containsKey(id)) {
				m.setMemberId(id);
				saveNewMember(m);
				return true;
			}
		}
		return false;
	}

	public Member srcMember(String id) {
		HashMap<String, Member> member = getMembers();
		for (Member record : member.values()) {
			if (record.getMemberId().equals(id)) {
				return record;
			}
		}
		return null;
	}

	// checkout
	@Override
	public List<CheckoutRecord> searchMember(String id) {
		HashMap<String, CheckoutRecord> checkoutRecord = getCheckoutRecords();
		List<CheckoutRecord> chkRecords = new ArrayList<CheckoutRecord>();
		for (CheckoutRecord record : checkoutRecord.values()) {
			if (record.getMember().getMemberId().equals(id)) {
				chkRecords.add(record);
			}
		}
		return chkRecords;
	}

	@Override
	public HashMap<String, CheckoutRecord> readCheckoutRecordMap() {
		return getCheckoutRecords();
	}

	@Override
	public boolean saveCheckoutRecord(CheckoutRecord record) {
		Book tempBook = record.getBook();
		if (tempBook.getNumberOfCopy() > 0 && record.isReturnStatus() == false) {
			tempBook.setNumberOfCopy(tempBook.getNumberOfCopy() - 1);
			saveNewBook(tempBook);
			writeToStorage(DbType.CHECKOUTRECORD, record);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean returnBook(String id) {
		HashMap<String, CheckoutRecord> bookMap = getCheckoutRecords();
		for (CheckoutRecord record : bookMap.values()) {
			if (record.getBook().getId().equals(id)) {
				LocalDate todayDate = LocalDate.now();
				if (todayDate.compareTo(record.getDueDate()) <= 0) {
					record.setReturnStatus(true);
					Book tempBook = record.getBook();
					tempBook.setNumberOfCopy(tempBook.getNumberOfCopy() + 1);
					saveNewBook(tempBook);
					saveCheckoutRecord(record);
				} else {
					System.out.println("Book is overdue, cannot checkout without fine!");
				}
			}
		}
		return false;
	}

}
