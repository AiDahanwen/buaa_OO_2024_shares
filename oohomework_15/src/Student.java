import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.HashMap;

import static java.lang.Math.min;

public class Student {
    private String studentId;
    private HashMap<LibraryBookId, Book> haveBooks;
    private HashMap<LibraryBookId, Book> dueBooks;
    private int credit;

    public Student(String id) {
        studentId = id;
        haveBooks = new HashMap<>();
        dueBooks = new HashMap<>();
        credit = 10;
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
        dueBooks.remove(bookId);
    }

    public Book getBook(LibraryBookId bookId) {
        return haveBooks.get(bookId);
    }

    public int getCredit() {
        return credit;
    }

    public void addCredit(int x) {
        credit = min(credit + x, 20);
    }

    public void subCredit(int y) {
        credit -= y;
    }

    public void fixCredit(LocalDate date) {
        for (Book book : haveBooks.values()) {
            if (book.borrowIsOverDue(date) && !dueBooks.containsKey(book.getBookId())) {
                subCredit(2);
                dueBooks.put(book.getBookId(), book);
            }
        }
    }
}
