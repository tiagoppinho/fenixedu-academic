package net.sourceforge.fenixedu.applicationTier.Servico.masterDegree.administrativeOffice.candidate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.fenixedu.applicationTier.IUserView;
import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.ExistingServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.InvalidChangeServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.gratuity.masterDegree.GratuityValuesNotDefinedServiceException;
import net.sourceforge.fenixedu.dataTransferObject.InfoCandidateRegistration;
import net.sourceforge.fenixedu.dataTransferObject.InfoEnrolment;
import net.sourceforge.fenixedu.dataTransferObject.InfoMasterDegreeCandidateWithInfoPerson;
import net.sourceforge.fenixedu.dataTransferObject.InfoStudentCurricularPlan;
import net.sourceforge.fenixedu.domain.Branch;
import net.sourceforge.fenixedu.domain.CandidateEnrolment;
import net.sourceforge.fenixedu.domain.CandidateSituation;
import net.sourceforge.fenixedu.domain.DegreeCurricularPlan;
import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.ExecutionPeriod;
import net.sourceforge.fenixedu.domain.GratuitySituation;
import net.sourceforge.fenixedu.domain.GratuityValues;
import net.sourceforge.fenixedu.domain.MasterDegreeCandidate;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.Qualification;
import net.sourceforge.fenixedu.domain.Role;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.curriculum.EnrollmentCondition;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.student.Registration;
import net.sourceforge.fenixedu.domain.studentCurricularPlan.StudentCurricularPlanState;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.util.SituationName;
import net.sourceforge.fenixedu.util.State;

import org.joda.time.YearMonthDay;

public class RegisterCandidate extends Service {

    public InfoCandidateRegistration run(Integer candidateID, Integer branchID, Integer studentNumber,
	    IUserView userView) throws FenixServiceException, ExcepcaoPersistencia {
	MasterDegreeCandidate masterDegreeCandidate = rootDomainObject
		.readMasterDegreeCandidateByOID(candidateID);

	Person person = masterDegreeCandidate.getPerson();

	checkCandidateSituation(masterDegreeCandidate.getActiveCandidateSituation());

	// remove master degree candidate role
	person.removeRoleByType(RoleType.MASTER_DEGREE_CANDIDATE);

	// check if old student number is free
	checkOldStudentNumber(studentNumber, person);

	// create new student
	Registration registration = createNewRegistration(person, studentNumber);

	// person.addPersonRoles(Role.getRoleByRoleType(RoleType.STUDENT));

	StudentCurricularPlan studentCurricularPlan = createNewStudentCurricularPlan(registration,
		branchID, masterDegreeCandidate);

	person.addPersonRoles(Role.getRoleByRoleType(RoleType.STUDENT));

	createEnrolments(userView, masterDegreeCandidate, studentCurricularPlan);

	updateCandidateSituation(masterDegreeCandidate);

	copyQualifications(masterDegreeCandidate, person);

	createGratuitySituation(masterDegreeCandidate, studentCurricularPlan);

	return createNewInfoCandidateRegistration(masterDegreeCandidate, studentCurricularPlan);

    }

    private InfoCandidateRegistration createNewInfoCandidateRegistration(
	    MasterDegreeCandidate masterDegreeCandidate, StudentCurricularPlan studentCurricularPlan) {
	InfoCandidateRegistration infoCandidateRegistration = new InfoCandidateRegistration();
	infoCandidateRegistration.setInfoMasterDegreeCandidate(InfoMasterDegreeCandidateWithInfoPerson
		.newInfoFromDomain(masterDegreeCandidate));
	infoCandidateRegistration.setInfoStudentCurricularPlan(InfoStudentCurricularPlan
		.newInfoFromDomain(studentCurricularPlan));
	infoCandidateRegistration.setEnrolments(new ArrayList<InfoEnrolment>());
	Iterator iteratorSCPs = studentCurricularPlan.getEnrolments().iterator();
	while (iteratorSCPs.hasNext()) {
	    Enrolment enrolment = (Enrolment) iteratorSCPs.next();
	    infoCandidateRegistration.getEnrolments().add(InfoEnrolment.newInfoFromDomain(enrolment));
	}
	return infoCandidateRegistration;
    }

    private void createGratuitySituation(MasterDegreeCandidate masterDegreeCandidate,
	    StudentCurricularPlan studentCurricularPlan) throws GratuityValuesNotDefinedServiceException {

	GratuityValues gratuityValues = masterDegreeCandidate.getExecutionDegree().getGratuityValues();

	if (gratuityValues == null) {
	    throw new GratuityValuesNotDefinedServiceException(
		    "error.exception.masterDegree.gratuity.gratuityValuesNotDefined");
	}

	new GratuitySituation(gratuityValues, studentCurricularPlan);
    }

    private void copyQualifications(MasterDegreeCandidate masterDegreeCandidate, Person person) {
	Qualification qualification = new Qualification();
	if (masterDegreeCandidate.getAverage() != null) {
	    qualification.setMark(masterDegreeCandidate.getAverage().toString());
	}
	qualification.setPerson(person);
	if (masterDegreeCandidate.getMajorDegreeSchool() == null) {
	    qualification.setSchool("");
	} else {
	    qualification.setSchool(masterDegreeCandidate.getMajorDegreeSchool());
	}
	qualification.setTitle(masterDegreeCandidate.getMajorDegree());

	Calendar calendar = Calendar.getInstance();
	if (masterDegreeCandidate.getMajorDegreeYear() == null) {
	    qualification.setDate(calendar.getTime());
	} else {
	    calendar.set(Calendar.YEAR, masterDegreeCandidate.getMajorDegreeYear().intValue());
	    qualification.setDate(calendar.getTime());
	}
	qualification.setDegree(masterDegreeCandidate.getMajorDegree());
    }

    private void updateCandidateSituation(MasterDegreeCandidate masterDegreeCandidate) {
	masterDegreeCandidate.getActiveCandidateSituation().setValidation(new State(State.INACTIVE));

	CandidateSituation candidateSituation = new CandidateSituation();
	candidateSituation.setDate(Calendar.getInstance().getTime());
	candidateSituation.setMasterDegreeCandidate(masterDegreeCandidate);
	candidateSituation.setValidation(new State(State.ACTIVE));
	candidateSituation.setSituation(SituationName.ENROLLED_OBJ);
    }

    private void createEnrolments(IUserView userView, MasterDegreeCandidate masterDegreeCandidate,
	    StudentCurricularPlan studentCurricularPlan) throws ExcepcaoPersistencia {
	List<CandidateEnrolment> candidateEnrolments = masterDegreeCandidate.getCandidateEnrolments();
	ExecutionPeriod executionPeriod = ExecutionPeriod.readActualExecutionPeriod();
	for (CandidateEnrolment candidateEnrolment : candidateEnrolments) {
	    new Enrolment(studentCurricularPlan, candidateEnrolment.getCurricularCourse(),
		    executionPeriod, EnrollmentCondition.FINAL, userView.getUtilizador());
	}
    }

    private StudentCurricularPlan createNewStudentCurricularPlan(Registration registration,
	    Integer branchID, MasterDegreeCandidate masterDegreeCandidate) throws ExcepcaoPersistencia {
	Branch branch = rootDomainObject.readBranchByOID(branchID);
	DegreeCurricularPlan degreecurricularPlan = masterDegreeCandidate.getExecutionDegree()
		.getDegreeCurricularPlan();

	StudentCurricularPlan studentCurricularPlan = new StudentCurricularPlan(registration, degreecurricularPlan, branch,
		new YearMonthDay(), masterDegreeCandidate.getGivenCredits(), masterDegreeCandidate.getSpecialization());
	return studentCurricularPlan;
    }

    private Registration createNewRegistration(Person person, Integer studentNumber)
	    throws ExcepcaoPersistencia {
	return new Registration(person, studentNumber);
    }

    private void checkOldStudentNumber(Integer studentNumber, Person person)
	    throws ExcepcaoPersistencia, ExistingServiceException {
	if (studentNumber != null) {

	    Registration existingStudent = Registration.readStudentByNumberAndDegreeType(studentNumber, DegreeType.MASTER_DEGREE);

	    if (existingStudent != null && !existingStudent.getPerson().equals(person)) {
		throw new ExistingServiceException();
	    }
	}
    }

    private void checkCandidateSituation(CandidateSituation situation)
	    throws InvalidChangeServiceException {
	if (situation.getSituation().equals(SituationName.ADMITIDO_OBJ)
		|| situation.getSituation().equals(SituationName.ADMITED_CONDICIONAL_CURRICULAR_OBJ)
		|| situation.getSituation().equals(SituationName.ADMITED_CONDICIONAL_FINALIST_OBJ)
		|| situation.getSituation().equals(SituationName.ADMITED_CONDICIONAL_OTHER_OBJ)
		|| situation.getSituation().equals(SituationName.ADMITED_SPECIALIZATION_OBJ)) {
	    return;
	}

	throw new InvalidChangeServiceException();
    }

}