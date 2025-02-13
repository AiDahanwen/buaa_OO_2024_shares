import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.oocourse.library1.LibrarySystem.PRINTER;
import static com.oocourse.library1.LibrarySystem.SCANNER;

public class Library {
    private QueryMachine queryMachine;
    private AppointmentOffice appointmentOffice;
    private CirculationDesk circulationDesk;
    private HashMap<String, Student> students;
    private List<LibraryMoveInfo> moveList;

    public Library() {
        HashMap<LibraryBookId, Integer> bookHouse =
                (HashMap<LibraryBookId, Integer>) SCANNER.getInventory();
        Shelf shelf = new Shelf(bookHouse);
        queryMachine = new QueryMachine(shelf);
        circulationDesk = new CirculationDesk(shelf);
        appointmentOffice = new AppointmentOffice(shelf);
        students = new HashMap<>();
        moveList = new ArrayList<>();
    }

    public void operate() {
        while (true) {
            LibraryCommand<?> command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            LocalDate date = command.getDate();
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
                LibraryRequest request = (LibraryRequest) command.getCmd();
                requestDispatch(date, request); //处理学生请求
            }
        }
    }

    public void moveToAppointmentOffice(LocalDate date) {
        appointmentOffice.moveIn(date, moveList);
    }

    public void moveFromCirculationDesk() {
        circulationDesk.moveOut(moveList);
    }

    public void moveFromAppointmentOffice(LocalDate date) {
        appointmentOffice.moveOut(date, moveList);
    }

    public void requestDispatch(LocalDate date, LibraryRequest request) {
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
                appointmentOffice.orderOneBook(date, student, bookId, request);
                break;
            case RETURNED:
                circulationDesk.returnOneBook(date, student, bookId, request);
                break;
            case PICKED:
                appointmentOffice.pickOneBook(date, student, bookId, request);
                break;
            default:
                break;
        }
    }
}
