import com.oocourse.library1.LibraryBookId;

import java.util.HashSet;

public class Student {
    private String studentId;
    private HashSet<LibraryBookId> haveBooks;

    public Student(String id) {
        studentId = id;
        haveBooks = new HashSet<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean haveB() {
        boolean flag = false;
        for (LibraryBookId bookId : haveBooks) {
            if (bookId.isTypeB()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean haveSameC(String uid) {
        boolean flag = false;
        for (LibraryBookId bookId : haveBooks) {
            if (bookId.isTypeC() && bookId.getUid().equals(uid)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void addOneBook(LibraryBookId bookId) {
        haveBooks.add(bookId);
    }

    public void subOneBook(LibraryBookId bookId) {
        haveBooks.remove(bookId);
    }
}
