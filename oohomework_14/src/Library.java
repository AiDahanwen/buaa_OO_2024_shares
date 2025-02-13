import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.annotation.Trigger;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import static com.oocourse.library2.LibrarySystem.PRINTER;
import static com.oocourse.library2.LibrarySystem.SCANNER;

public class Library {
    private QueryMachine queryMachine;
    private AppointmentOffice appointmentOffice;
    private CirculationDesk circulationDesk;
    private HashMap<String, Student> students;
    private List<LibraryMoveInfo> moveList;
    private DriftCorner driftCorner;

    public Library() {
        Shelf shelf = initShelf();
        driftCorner = new DriftCorner();
        queryMachine = new QueryMachine(shelf, driftCorner);
        circulationDesk = new CirculationDesk(shelf, driftCorner);
        appointmentOffice = new AppointmentOffice(shelf);
        students = new HashMap<>();
        moveList = new ArrayList<>();
    }

    public void operate() {
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate date = command.getDate(); // 今天的日期
            if (command.getCmd().equals("OPEN")) {
                moveList.clear();
                moveFromAppointmentOffice(date); //将预约处逾期的书放回书架
                moveToAppointmentOffice(date); //将有预约请求的书移到预约处
                PRINTER.move(date, moveList);
            } else if (command.getCmd().equals("CLOSE")) {
                moveList.clear();
                moveFromCirculationDesk(); //将借还处剩余的书放回书架
                PRINTER.move(date, moveList);
            } else {
                requestDispatch(date, (LibraryReqCmd) command); //处理学生请求
            }
        }
    }

    @Trigger(from = "BS", to = "AO")
    public void moveToAppointmentOffice(LocalDate date) {
        appointmentOffice.moveIn(date, moveList);
    }

    @Trigger(from = "BRO", to = "BS")
    public void moveFromCirculationDesk() {
        circulationDesk.moveOut(moveList);
    }

    @Trigger(from = "AO", to = "BS")
    public void moveFromAppointmentOffice(LocalDate date) {
        appointmentOffice.moveOut(date, moveList);
    }

    public void requestDispatch(LocalDate date, LibraryReqCmd request) {
        String studentId = request.getStudentId();
        if (!students.containsKey(studentId)) {
            Student student = new Student(request.getStudentId());
            students.put(student.getStudentId(), student);
        }
        Student student = students.get(request.getStudentId());
        LibraryBookId bookId = request.getBookId();
        LibraryRequest.Type type = request.getType();
        switch (type) {
            case QUERIED:
                queryMachine.queryLeftBook(date, bookId);
                break;
            case BORROWED:
                circulationDesk.borrowOneBook(date, student, bookId, request);
                break;
            case ORDERED:
                appointmentOffice.orderOneBook(student, bookId, request);
                break;
            case RETURNED:
                circulationDesk.returnOneBook(date, student, bookId, request);
                break;
            case PICKED:
                appointmentOffice.pickOneBook(date, student, bookId, request);
                break;
            case DONATED:
                driftCorner.donateOneBook(bookId, request);
                break;
            case RENEWED:
                appointmentOffice.renewedOneBook(date, bookId, student, request);
                break;
            default:
                break;
        }
    }

    @Trigger(from = "InitState", to = "BS")
    public Shelf initShelf() {
        HashMap<LibraryBookId, Integer> bookHouse =
                (HashMap<LibraryBookId, Integer>) SCANNER.getInventory();
        return new Shelf(bookHouse);
    }
}
