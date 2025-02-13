import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.oocourse.library2.LibrarySystem.PRINTER;

public class AppointmentOffice {
    private ArrayList<LibraryRequest> requests; //预约请求表
    private ArrayList<Book> validBooks; //储存书，只用于删除书
    private HashMap<String, HashMap<LibraryBookId, Integer>> storeBooks; //未过期的书，用于取书
    private Shelf shelf;

    public AppointmentOffice(Shelf shelf) {
        requests = new ArrayList<>();
        validBooks = new ArrayList<>();
        storeBooks = new HashMap<>();
        this.shelf = shelf;
    }

    public void orderOneBook(Student student,
                             LibraryBookId bookId, LibraryReqCmd request) {
        boolean fail = false;
        if (bookId.isTypeA()) {
            fail = true;
        } else if (bookId.isTypeB() && student.haveB()) {
            fail = true;
        } else if (bookId.isTypeC() && student.haveSameC(bookId.getUid())) {
            fail = true;
        }
        if (!bookId.isFormal()) {
            fail = true;
        }
        if (fail) {
            PRINTER.reject(request);
        } else {
            PRINTER.accept(request);
            requests.add(request.getRequest());
        }
    }

    @Trigger(from = "AO", to = "STU")
    public void pickOneBook(LocalDate date, Student student,
                            LibraryBookId bookId, LibraryReqCmd request) {
        boolean fail = false;
        String studentId = student.getStudentId();
        if (!storeBooks.containsKey(studentId)) {
            fail = true;
        } else {
            HashMap<LibraryBookId, Integer> orderBooks = storeBooks.get(studentId);
            if (!orderBooks.containsKey(bookId)) { //没有该书号
                fail = true;
            } else if (orderBooks.get(bookId) == 0) { //书号对应的书数目为0
                fail = true;
            }
        }
        if (bookId.isTypeA()) {
            fail = true;
        } else if (bookId.isTypeB() && student.haveB()) {
            fail = true;
        } else if (bookId.isTypeC() && student.haveSameC(bookId.getUid())) {
            fail = true;
        }
        if (fail) {
            PRINTER.reject(request);
        } else {
            PRINTER.accept(request);
            student.addOneBook(date, bookId, null);
            bookRemove(studentId, bookId);
            int old = storeBooks.get(studentId).get(bookId);
            storeBooks.get(studentId).put(bookId, old - 1);
        }
    }

    public void moveIn(LocalDate date, List<LibraryMoveInfo> moveList) {
        Iterator<LibraryRequest> iterator = requests.iterator();
        while (iterator.hasNext()) {
            LibraryRequest request = iterator.next();
            LibraryBookId bookId = request.getBookId();
            String studentId = request.getStudentId();
            if (!bookId.isTypeA() && shelf.haveLeft(bookId)) {
                Book book = new Book(date, bookId, studentId);
                validBooks.add(book);
                if (!storeBooks.containsKey(studentId)) {
                    storeBooks.put(studentId, new HashMap<>());
                }
                HashMap<LibraryBookId, Integer> orderedBooks = storeBooks.get(studentId);
                if (orderedBooks.containsKey(bookId)) {
                    orderedBooks.put(bookId, orderedBooks.get(bookId) + 1);
                } else {
                    orderedBooks.put(bookId, 1);
                }
                shelf.outOneBook(bookId);
                moveList.add(new LibraryMoveInfo(bookId, "bs", "ao", studentId));
                iterator.remove();
            }
        }
    }

    public void moveOut(LocalDate date, List<LibraryMoveInfo> moveList) {
        Iterator<Book> iterator = validBooks.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.orderIsOverDue(date)) {
                iterator.remove();
                LibraryBookId bookId = book.getBookId();
                shelf.inOneBook(bookId);
                moveList.add(new LibraryMoveInfo(bookId, "ao", "bs"));
                String s = book.getStudentId();
                HashMap<LibraryBookId, Integer> orderBooks = storeBooks.get(s);
                orderBooks.put(bookId, orderBooks.get(bookId) - 1);
            }
        }
    }

    public void bookRemove(String studentId, LibraryBookId bookId) {
        Iterator<Book> iterator = validBooks.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getBookId().equals(bookId) && book.getStudentId().equals(studentId)) {
                iterator.remove();
                break;
            }
        }
    } //去掉最早被送到的那本书

    public void renewedOneBook(LocalDate date, LibraryBookId bookId,
                               Student student, LibraryReqCmd reqCmd) {
        boolean fail = false;
        if (student.getBook(bookId).renewIsOverDue(date)) {
            fail = true;
        }
        if (!bookId.isFormal()) {
            fail = true;
        }
        for (LibraryRequest request : requests) {
            if (request.getBookId().equals(bookId) && !shelf.haveLeft(bookId)) {
                fail = true;
                break;
            }
        }
        for (HashMap<LibraryBookId, Integer> map : storeBooks.values()) {
            for (LibraryBookId bookId1 : map.keySet()) {
                if (bookId1.equals(bookId) && (map.get(bookId1) != 0) && !shelf.haveLeft(bookId)) {
                    fail = true;
                    break;
                }
            }
            if (fail) {
                break;
            }
        }
        if (fail) {
            PRINTER.reject(reqCmd);
        } else {
            PRINTER.accept(reqCmd);
            student.getBook(bookId).addBorrowPeriod();
        }
    }
}
