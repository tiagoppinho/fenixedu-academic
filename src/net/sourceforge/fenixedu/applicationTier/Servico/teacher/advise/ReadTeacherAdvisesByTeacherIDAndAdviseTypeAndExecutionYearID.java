/**
 * 
 *
 */
package net.sourceforge.fenixedu.applicationTier.Servico.teacher.advise;

import java.util.List;

import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.teacher.Advise;
import net.sourceforge.fenixedu.domain.teacher.AdviseType;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentExecutionYear;
import net.sourceforge.fenixedu.persistenceTier.IPersistentTeacher;

/**
 * @author naat
 * 
 */
public class ReadTeacherAdvisesByTeacherIDAndAdviseTypeAndExecutionYearID extends Service {

    public List<Advise> run(AdviseType adviseType, Integer teacherID, Integer executionYearID)
            throws ExcepcaoPersistencia, FenixServiceException, DomainException {
        IPersistentTeacher persistentTeacher = persistentSupport.getIPersistentTeacher();
        Teacher teacher = (Teacher) persistentTeacher.readByOID(Teacher.class, teacherID);
        List<Advise> result;

        if (executionYearID != null) {
            IPersistentExecutionYear persistentExecutionYear = persistentSupport
                    .getIPersistentExecutionYear();
            ExecutionYear executionYear = (ExecutionYear) persistentExecutionYear.readByOID(
                    ExecutionYear.class, executionYearID);

            result = teacher.getAdvisesByAdviseTypeAndExecutionYear(adviseType, executionYear);
        } else {
            result = teacher.getAdvisesByAdviseType(adviseType);
        }

        return result;

    }
}