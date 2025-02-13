import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;

public class Book {
    private LibraryBookId bookId;
    private LocalDate arriveTime;
    private String studentId;
    private LocalDate borrowTime;
    private int borrowPeriod;
    private int times;
    private int num;

    public Book(LocalDate date, LibraryBookId bookId, String studentId) {
        this.arriveTime = date;
        this.bookId = bookId;
        this.studentId = studentId;
    }

    public Book(LocalDate date, LibraryBookId bookId) {
        this.bookId = bookId;
        this.borrowTime = date;
        if (bookId.isTypeB()) {
            borrowPeriod = 30;
        } else if (bookId.isTypeC()) {
            borrowPeriod = 60;
        } else if (bookId.isTypeCU()) {
            borrowPeriod = 14;
        } else if (bookId.isTypeBU()) {
            borrowPeriod = 7;
        }
    }

    public Book(LibraryBookId bookId) {
        this.bookId = bookId;
        this.times = 0;
        this.num = 1;
        if (bookId.isTypeBU()) {
            borrowPeriod = 7;
        } else if (bookId.isTypeCU()) {
            borrowPeriod = 14;
        }
    }

    public boolean orderIsOverDue(LocalDate nowTime) {
        int dayStart = arriveTime.getDayOfYear();
        int dayEnd = nowTime.getDayOfYear();
        int days = dayEnd - dayStart;
        return days >= 5;
    }

    public boolean borrowIsOverDue(LocalDate returnTime) {
        int dayBorrow = borrowTime.getDayOfYear();
        int dayReturn = returnTime.getDayOfYear();
        int days = dayReturn - dayBorrow;
        return days > borrowPeriod;
    }

    public boolean renewIsOverDue(LocalDate renewTime) {
        int dayRenew = renewTime.getDayOfYear();
        int dayDue = borrowTime.getDayOfYear() + borrowPeriod;
        if (dayRenew > dayDue) {
            return true;
        } else {
            return dayDue - dayRenew >= 5;
        }
    }

    public void addTimes() {
        times++;
    }

    public boolean isTwoTimes() {
        return times == 2;
    }

    public void addBorrowPeriod() {
        borrowPeriod += 30;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getNumber() {
        return this.num;
    }

    public void subNum() {
        num--;
    }

    public void addNum() {
        num++;
    }

    public void setBorrowTime(LocalDate date) {
        borrowTime = date;
    }
}
