/*
 * Created on 15/Mai/2003 by jpvl
 *
 */
package ServidorApresentacao.Action.teacher.credits;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.validator.DynaValidatorForm;

import DataBeans.InfoExecutionCourse;
import DataBeans.InfoShift;
import DataBeans.InfoTeacher;
import DataBeans.teacher.credits.InfoShiftPercentage;
import DataBeans.teacher.credits.InfoTeacherShiftPercentage;
import ServidorAplicacao.IUserView;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionConstants;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author jpvl
 */
public class ProfessorShipShiftPercentageDispatchAction
	extends DispatchAction {

	public ActionForward show(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception {
		DynaValidatorForm professorshipShiftPercentageForm =
			(DynaValidatorForm) form;

		InfoExecutionCourse infoExecutionCourse = new InfoExecutionCourse();
		Integer idInternal =
			(Integer) professorshipShiftPercentageForm.get("objectCode");
		infoExecutionCourse.setIdInternal(idInternal);

		HttpSession session = request.getSession();
		IUserView userView = SessionUtils.getUserView(request);
		InfoTeacher infoTeacher =
			(InfoTeacher) session.getAttribute(SessionConstants.INFO_TEACHER);

		Object args[] = { infoTeacher, infoExecutionCourse };

		List infoShiftPercentageList =
			(List) ServiceUtils.executeService(
				userView,
				"ReadTeacherExecutionCourseShiftsPercentage",
				args);

		Collections.sort(infoShiftPercentageList, new Comparator() {

			public int compare(Object o1, Object o2) {
				InfoShiftPercentage infoShiftPercentage1 =
					(InfoShiftPercentage) o1;
				InfoShiftPercentage infoShiftPercentage2 =
					(InfoShiftPercentage) o2;

				Integer type1 =
					infoShiftPercentage1.getShift().getTipo().getTipo();
				Integer type2 =
					infoShiftPercentage2.getShift().getTipo().getTipo();

				return type1.intValue() - type2.intValue();
			}
		});
		
		request.setAttribute(
			"infoShiftPercentageList",
			infoShiftPercentageList);
		return mapping.findForward("showTable");
	}

	public ActionForward accept(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception {

		HttpSession session = request.getSession();

		IUserView userView = SessionUtils.getUserView(request);
		InfoTeacher infoTeacher =
			(InfoTeacher) session.getAttribute(SessionConstants.INFO_TEACHER);

		List infoTeacherShiftPercentageList =
			processForm((DynaActionForm) form, request);

		Object[] args =
			{
				infoTeacher,
				getInfoExecutionCourse((DynaActionForm) form),
				infoTeacherShiftPercentageList };
		List shiftWithErrors =
			(List) ServiceUtils.executeService(
				userView,
				"AcceptTeacherExecutionCourseShiftPercentage",
				args);

		if (shiftWithErrors.size() > 0) {
			ActionErrors actionErrors = new ActionErrors();

			Iterator iterator = shiftWithErrors.listIterator();
			while (iterator.hasNext()) {
				InfoShift infoShift = (InfoShift) iterator.next();
				actionErrors.add(
					"shiftPercentage",
					new ActionError(
						"errors.ShiftPercentage",
						infoShift.getNome()));
			}

			saveErrors(request, actionErrors);

			// TODO Para que � que se est� a fazer isto?
			request.setAttribute(
				"objectCode",
				getInfoExecutionCourse((DynaActionForm) form).getIdInternal());

			return mapping.getInputForward();
		}

		return mapping.findForward("acceptSuccess");
	}

	private InfoExecutionCourse getInfoExecutionCourse(DynaActionForm form) {
		InfoExecutionCourse infoExecutionCourse = new InfoExecutionCourse();
		infoExecutionCourse.setIdInternal((Integer) form.get("objectCode"));
		return infoExecutionCourse;
	}


	private List processForm(DynaActionForm form, HttpServletRequest request) {
		List infoTeacherShiftPercentageList = new ArrayList();

		InfoTeacherShiftPercentage infoTeacherShiftPercentage =
			new InfoTeacherShiftPercentage();

		Integer[] shiftProfessorships =
			(Integer[]) form.get("shiftProfessorships");

		DecimalFormatSymbols defaultDecimalFormats = new DecimalFormatSymbols();
		System.out.println(defaultDecimalFormats.getDecimalSeparator());
		for (int i = 0; i < shiftProfessorships.length; i++) {
			Integer shiftInternalCode = shiftProfessorships[i];
			if (shiftInternalCode != null) {
				Double percentage = null;
				try {
					System.out.println("Ola=======================");
					percentage =
						new Double(
							Double.parseDouble(
								request.getParameter(
									"percentage_" + shiftInternalCode)));
				} catch (NumberFormatException e) {
					e.printStackTrace(System.out);
					
				}

				if (percentage != null) {
					infoTeacherShiftPercentage =
						new InfoTeacherShiftPercentage();
					infoTeacherShiftPercentage.setPercentage(percentage);

					InfoShift infoShift = new InfoShift();
					infoShift.setIdInternal(shiftInternalCode);
					infoTeacherShiftPercentage.setInfoShift(infoShift);
					infoTeacherShiftPercentageList.add(
						infoTeacherShiftPercentage);
				}
			}
		}

		return infoTeacherShiftPercentageList;
	}
}
