import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.HashMap;

public class Student {
    private String studentId;
    private HashMap<LibraryBookId, Book> haveBooks;

    public Student(String id) {
        studentId = id;
        haveBooks = new HashMap<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean haveB() {
        boolean flag = false;
        for (LibraryBookId bookId : haveBooks.keySet()) {
            if (bookId.isTypeB()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean haveBU() {
        boolean flag = false;
        for (LibraryBookId bookId : haveBooks.keySet()) {
            if (bookId.isTypeBU()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean haveSameC(String uid) {
        boolean flag = false;
        for (LibraryBookId bookId : haveBooks.keySet()) {
            if ((bookId.isTypeC() || bookId.isTypeCU()) && bookId.getUid().equals(uid)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Trigger(from = "BS", to = "STU")
    public void addOneBook(LocalDate date, LibraryBookId bookId, Book book) {
        if (book == null) {
            haveBooks.put(bookId, new Book(date, bookId));
        } else {
            addInformalBook(date, bookId, book);
        }
    }

    @Trigger(from = "BDC", to = "STU")
    public void addInformalBook(LocalDate date, LibraryBookId bookId, Book book) {
        haveBooks.put(bookId, book);
        book.setBorrowTime(date);
    }

    public void subOneBook(LibraryBookId bookId) {
        haveBooks.remove(bookId);
    }

    public Book getBook(LibraryBookId bookId) {
        return haveBooks.get(bookId);
    }
}
