import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.oocourse.library3.LibrarySystem.PRINTER;

public class CirculationDesk {
    private Shelf shelf;
    private DriftCorner driftCorner;
    private HashMap<LibraryBookId, Integer> leftBooks;
    private HashSet<LibraryBookId> upgradeBooks;
    private HashSet<LibraryBookId> returnDriftBooks;
    private HashMap<String, Student> students;

    public CirculationDesk(Shelf shelf, DriftCorner driftCorner,
                           HashMap<String, Student> students) {
        this.shelf = shelf;
        this.driftCorner = driftCorner;
        leftBooks = new HashMap<>();
        upgradeBooks = new HashSet<>();
        returnDriftBooks = new HashSet<>();
        this.students = students;
    }

    @SendMessage(from = "Library", to = "CirculationDesk")
    public void borrowOneBook(LocalDate date, Student student,
                              LibraryBookId bookId, LibraryReqCmd request) {
        boolean fail = false;
        if (bookId.isFormal()) {
            if (shelf.getBookNumber(bookId) == 0) {
                fail = true;
            }
        } else {
            if (driftCorner.getBookNumber(bookId) == 0) {
                fail = true;
            }
        }
        if (bookId.isTypeA() || bookId.isTypeAU()) {
            fail = true;
        }
        if (bookId.isTypeB()) {
            if (student.haveB()) {
                fail = true;
            }
        }
        if (bookId.isTypeBU()) {
            if (student.haveBU()) {
                fail = true;
            }
        }
        if (bookId.isTypeC() || bookId.isTypeCU()) {
            if (student.haveSameC(bookId.getUid())) {
                fail = true;
            }
        }
        if (student.getCredit() < 0) {
            fail = true;
        }
        Book book = null;
        if (fail) { //失败则等着放回去
            PRINTER.reject(request);
            if (bookId.isFormal()) {
                if (!bookId.isTypeA() && shelf.haveLeft(bookId)) {
                    shelf.outOneBook(bookId);
                    addOneBookToHashMap(bookId, leftBooks);
                }
            } else {
                if (!bookId.isTypeAU() && driftCorner.haveLeft(bookId)) {
                    Book temp = driftCorner.outOneBook(bookId);
                    addOneDriftBook(bookId);
                }
            }
        } else {
            if (bookId.isFormal()) {
                shelf.outOneBook(bookId);
            } else {
                book = driftCorner.outOneBook(bookId);
            }
            student.addOneBook(date, bookId, book); //成功就直接给student
            PRINTER.accept(request);
        }
    }

    @SendMessage(from = "Library", to = "CirculationDesk")
    @Trigger(from = "STU", to = "BRO")
    public void returnOneBook(LocalDate date, Student student,
                              LibraryBookId bookId, LibraryReqCmd request) {
        if (bookId.isFormal()) {
            addOneBookToHashMap(bookId, leftBooks);
        } else {
            student.getBook(bookId).addTimes();
            if (student.getBook(bookId).isTwoTimes()) {
                upgradeBooks.add(bookId);
            } else {
                returnDriftBooks.add(bookId);
            }
        }
        if (student.getBook(bookId).borrowIsOverDue(date)) {
            PRINTER.accept(request, "overdue");
        } else {
            student.addCredit(1);
            PRINTER.accept(request, "not overdue");
        }
        student.subOneBook(bookId);
    }

    @SendMessage(from = "Library", to = "CirculationDesk")
    public void moveOutC(List<LibraryMoveInfo> moveList) {
        for (LibraryBookId bookId : leftBooks.keySet()) {
            int number = leftBooks.get(bookId);
            for (int i = 0; i < number; i++) {
                shelf.inOneBook(bookId); //注意每条都要对应一个move
                moveList.add(new LibraryMoveInfo(bookId, "bro", "bs"));
            }
        }
        leftBooks.clear();
        for (LibraryBookId bookId : returnDriftBooks) {
            driftCorner.returnBook(bookId);
            moveList.add(new LibraryMoveInfo(bookId, "bro", "bdc"));
        }
        returnDriftBooks.clear();
        for (LibraryBookId bookId : upgradeBooks) {
            String id = driftCorner.getOneBook(bookId).getStudentId();
            students.get(id).addCredit(2);
            shelf.newOneBook(bookId);
            driftCorner.removeOneBook(bookId);
            moveList.add(new LibraryMoveInfo(bookId, "bro", "bs"));
        }
        upgradeBooks.clear();
    }

    @Trigger(from = "BS", to = "BRO")
    public void addOneBookToHashMap(LibraryBookId bookId, HashMap<LibraryBookId, Integer> map) {
        if (map.containsKey(bookId)) {
            int old = map.get(bookId);
            map.put(bookId, old + 1);
        } else {
            map.put(bookId, 1);
        }
    }

    @Trigger(from = "BDC", to = "BRO")
    public void addOneDriftBook(LibraryBookId bookId) {
        returnDriftBooks.add(bookId);
    }
}
