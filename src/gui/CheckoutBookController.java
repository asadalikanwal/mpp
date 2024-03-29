package gui;

import java.util.List;

import business.Book;
import business.CheckoutRecord;
import business.Member;
import dataaccess.DataAccessFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CheckoutBookController {

	Book book;
	Member member;
	DataAccessFacade daf = new DataAccessFacade();

	@FXML
	private TextField fxMemberID;

	@FXML
	private TextField fxBookId;

	@FXML
	private Label fxMemberIDError;

	@FXML
	private Label fxBookIdError;

	@FXML
	private TableView<CheckoutRecord> result;

	@FXML
	private TableColumn tbMemberId;

	@FXML
	private TableColumn tbMemberName;

	@FXML
	private TableColumn tbBookName;

	@FXML
	private TableColumn tbISBN;

	@FXML
	private TableColumn tbBookID;

	@FXML
	private TableColumn tbDate;

	@FXML
	private TableColumn tbStatus;

	@FXML
	private Button fxCheckoutBttn;

	@FXML
	void CheckoutBookEvent(ActionEvent event) {

		CheckoutRecord record = new CheckoutRecord(member, book);

		List<CheckoutRecord> records = daf.searchMember(member.getMemberId());

		for (CheckoutRecord rec : records) {
			if (rec.getBook().getIsbn().equals(fxBookId.getText())) {
				fxBookIdError.setText("This book is already issued for this member.");
				fxBookIdError.setTextFill(javafx.scene.paint.Color.RED);
				fxBookIdError.setVisible(true);
				return;
			}
		}

		boolean checkout = daf.saveCheckoutRecord(record);
		List<CheckoutRecord> recordsNew = daf.searchMember(member.getMemberId());

		result.getItems().setAll(recordsNew);
		tbMemberId.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("memberID"));
		tbMemberName.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("memberName"));
		tbBookName.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookTitle"));
		tbISBN.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookISBN"));
		tbBookID.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookId"));
		tbDate.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("dueDate"));
		tbStatus.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("returnStatus"));

	}

	@FXML
	void checkAvailability(ActionEvent event) {
		boolean bookCheck = Validation.isValid(fxBookId.getText(), "String", fxBookIdError);
		if (!bookCheck) {
			return;
		}

		book = daf.searchBook(fxBookId.getText());
		if (book != null) {
			int availability = book.getNumberOfCopy();
			if (availability < 1) {
				fxBookIdError.setText("Book not available");
				fxBookIdError.setTextFill(javafx.scene.paint.Color.RED);
				fxBookIdError.setVisible(true);
				return;
			} else {
				fxBookIdError.setText("Book Available: " + book.getTitle());
				fxBookIdError.setTextFill(javafx.scene.paint.Color.GREEN);
				fxBookIdError.setVisible(true);
			}
		}

		if (book == null) {
			fxBookIdError.setText("Book not available");
			fxBookIdError.setTextFill(javafx.scene.paint.Color.RED);
			fxBookIdError.setVisible(true);
			return;
		}

		checkoutButtonEnable();

	}

	@FXML
	void searchMember(ActionEvent event) {
		boolean memberCheck = Validation.isValid(fxMemberID.getText(), "Number", fxMemberIDError);

		if (!memberCheck) {
			return;
		}

		member = daf.srcMember(fxMemberID.getText());

		if (member == null) {
			fxMemberIDError.setText("Member not found");
			fxMemberIDError.setVisible(true);

			return;
		} else {
			fxMemberIDError.setVisible(false);
		}
		checkoutButtonEnable();
		populateTable();

	}

	private void populateTable() {

		List<CheckoutRecord> records = daf.searchMember(member.getMemberId());

		result.getItems().setAll(records);
		tbMemberId.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("memberID"));
		tbMemberName.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("memberName"));
		tbBookName.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookTitle"));
		tbISBN.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookISBN"));
		tbBookID.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("bookId"));
		tbDate.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("dueDate"));
		tbStatus.setCellValueFactory(new PropertyValueFactory<CheckoutRecord, String>("returnStatus"));

	}

	@FXML
	void checkoutButtonEnable() {
		Book b = daf.searchBook(fxBookId.getText());
		if (b != null) {
			int availability = b.getNumberOfCopy();
			if (availability < 1) {
				fxCheckoutBttn.setDisable(true);
				return;
			} else {
				fxCheckoutBttn.setDisable(false);
			}
		}

		if (b == null || daf.srcMember(fxMemberID.getText()) == null) {
			fxCheckoutBttn.setDisable(true);
		} else {
			fxCheckoutBttn.setDisable(false);
		}
	}

	public void init() {
		CheckoutBook.INSTANCE.setTitle("Checkout Book");
		fxMemberID.requestFocus();
		fxCheckoutBttn.setDisable(true);

	}
}