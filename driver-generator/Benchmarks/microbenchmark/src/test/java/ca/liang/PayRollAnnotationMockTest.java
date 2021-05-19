package ca.liang;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/** This class is a microbenchmark which tests detection of mockito annotations. */
//@RunWith(MockitoJUnitRunner.class)
public class PayRollAnnotationMockTest {
    
    private PayRoll payRoll;
    
    @Mock
    private EmployeeDB employeeDB;
    
    @Mock
    private BankService bankService;

    private Employee[] employees;
    
    // Contains mock object (initialized through annotation)
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        payRoll = new PayRoll(employeeDB, bankService);
    }
    // total mock calls: 0
    
    @Test
    public void testNoEmployees() {
        assertNumberOfPayments(0);
    }
    // total mock calls: 0
    
    // Contains mock object
    @Test
    public void testEmployeesPaidIntra() {
        Employee[] employees_intra = new Employee[2];
        employees_intra[0] = mock(Employee.class);
        employees_intra[1] = mock(Employee.class);
        
        EmployeeDB employeeDB_intra = new EmployeeDB(Arrays.asList(employees_intra));
        PayRoll payRoll_intra = new PayRoll(employees_intra, bankService);

	// not mock
        int numberOfPayments = payRoll_intra.monthlyPayment();
        assertEquals(2, numberOfPayments);
    }
    // total mock calls: 0
    
    // Contains mock object (passed in from field)
    @Test
    public void testSingleEmployee() {
        employees = new Employee[1];
        employees[0] = createTestEmployee("Test Employee", "ID0", 1000);

        // *mock*
        when(employeeDB.getAllEmployees()).thenReturn((List<Employee>) Arrays.asList(employees));

        payRoll = new PayRoll(employeeDB, bankService);

        assertNumberOfPayments(1);
    }
    // total mock calls: 1
    
    // Contains mock object (one passed in from field, and the other created from verify() )
    @Test
    public void testEmployeeIsPaid() {
        employees = new Employee[1];
        String employeeId = "ID0";
        int salary = 1000;
        employees[0] = createTestEmployee("Test Employee", employeeId, salary);

        // *mock* from field
        when(employeeDB.getAllEmployees()).thenReturn((List<Employee>) Arrays.asList(employees));

        payRoll = new PayRoll(employeeDB, bankService);
        
        assertNumberOfPayments(1);

        // *mock* return value from verify()
        verify(bankService, times(1)).makePayment(employeeId, salary);
    }
    // total mock calls: 2
    
    private void assertNumberOfPayments(int expected) {
        int numberOfPayments = payRoll.monthlyPayment();
        assertEquals(expected, numberOfPayments);
    }
    // total mock calls: 0

    private Employee createTestEmployee(String name, String id, int salary) {
        return new Employee(name, id, salary);
    }
    // total mock calls: 0
}
