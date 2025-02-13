import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private LibraryBookId bookId;
    private LocalDate arriveTime;
    private String studentId;

    public Book(LocalDate date, LibraryBookId bookId, String studentId) {
        this.arriveTime = date;
        this.bookId = bookId;
        this.studentId = studentId;
    }

    public boolean isOverDue(LocalDate nowTime) {
        int dayStart = arriveTime.getDayOfYear();
        int dayEnd = nowTime.getDayOfYear();
        int days = dayEnd - dayStart;
        return days >= 5;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public String getStudentId() {
        return studentId;
    }
}
