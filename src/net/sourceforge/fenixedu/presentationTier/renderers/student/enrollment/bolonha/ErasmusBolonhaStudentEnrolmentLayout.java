package net.sourceforge.fenixedu.presentationTier.renderers.student.enrollment.bolonha;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import net.sourceforge.fenixedu.dataTransferObject.student.enrollment.bolonha.BolonhaStudentEnrollmentBean;
import net.sourceforge.fenixedu.dataTransferObject.student.enrollment.bolonha.ErasmusBolonhaStudentEnrollmentBean;
import net.sourceforge.fenixedu.dataTransferObject.student.enrollment.bolonha.ErasmusBolonhaStudentEnrollmentBean.ErasmusExtraCurricularEnrolmentBean;
import net.sourceforge.fenixedu.dataTransferObject.student.enrollment.bolonha.StudentCurriculumGroupBean;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.DegreeCurricularPlan;
import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.accessControl.GroupTypes;
import net.sourceforge.fenixedu.domain.enrolment.IDegreeModuleToEvaluate;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.studentCurriculum.NoCourseGroupCurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.NoCourseGroupCurriculumGroupType;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.presentationTier.renderers.converters.DomainObjectKeyArrayConverter;
import net.sourceforge.fenixedu.presentationTier.renderers.converters.DomainObjectKeyConverter;
import net.sourceforge.fenixedu.presentationTier.renderers.student.enrollment.bolonha.BolonhaStudentEnrolmentLayout.OptionalCurricularCourseLinkController;
import pt.ist.fenixWebFramework.rendererExtensions.controllers.CopyCheckBoxValuesController;
import pt.ist.fenixWebFramework.renderers.components.HtmlActionLink;
import pt.ist.fenixWebFramework.renderers.components.HtmlBlockContainer;
import pt.ist.fenixWebFramework.renderers.components.HtmlCheckBox;
import pt.ist.fenixWebFramework.renderers.components.HtmlComponent;
import pt.ist.fenixWebFramework.renderers.components.HtmlContainer;
import pt.ist.fenixWebFramework.renderers.components.HtmlMultipleHiddenField;
import pt.ist.fenixWebFramework.renderers.components.HtmlTable;
import pt.ist.fenixWebFramework.renderers.components.HtmlTableCell;
import pt.ist.fenixWebFramework.renderers.components.HtmlTableRow;
import pt.ist.fenixWebFramework.renderers.components.HtmlText;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;
import pt.ist.fenixWebFramework.renderers.model.MetaObject;
import pt.ist.fenixWebFramework.renderers.model.MetaObjectFactory;
import pt.ist.fenixWebFramework.renderers.schemas.Schema;
import pt.utl.ist.fenix.tools.spreadsheet.styles.CellBorder;
import pt.utl.ist.fenix.tools.util.StringAppender;

public class ErasmusBolonhaStudentEnrolmentLayout extends BolonhaStudentEnrolmentLayout {

    protected boolean isAcademicAdminOfficeEmployee() {
	final Person person = AccessControl.getPerson();
	return person.hasRole(RoleType.INTERNATIONAL_RELATION_OFFICE) || super.isAcademicAdminOfficeEmployee();
    }

    private boolean contains(List<CurricularCourse> curricularCourseList, final IDegreeModuleToEvaluate degreeModule) {
	if (!CurricularCourse.class.isAssignableFrom(degreeModule.getClass())) {
	    return false;
	}

	return CollectionUtils.find(curricularCourseList, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		return ((CurricularCourse) degreeModule).isEquivalent((CurricularCourse) arg0);
	    }
	}) != null;
    }

    protected void generateCurricularCoursesToEnrol(HtmlTable groupTable, StudentCurriculumGroupBean studentCurriculumGroupBean) {
	final List<IDegreeModuleToEvaluate> coursesToEvaluate = studentCurriculumGroupBean.getSortedDegreeModulesToEvaluate();
	generateCurricularCoursesToEnrol(groupTable, coursesToEvaluate);
    }

    private void generateCurricularCoursesToEnrol(HtmlTable groupTable, final List<IDegreeModuleToEvaluate> coursesToEvaluate) {
	ErasmusBolonhaStudentEnrollmentBean bean = (ErasmusBolonhaStudentEnrollmentBean) getBolonhaStudentEnrollmentBean();

	for (final IDegreeModuleToEvaluate degreeModuleToEvaluate : coursesToEvaluate) {
	    if (!bean.getCandidacy().getCurricularCourses().contains(degreeModuleToEvaluate.getDegreeModule())) {
		// if(contains(bean.getCandidacy().getCurricularCourses(),
		// degreeModuleToEvaluate)) {
		continue;
	    }

	    HtmlTableRow htmlTableRow = groupTable.createRow();
	    HtmlTableCell cellName = htmlTableRow.createCell();
	    cellName.setClasses(getRenderer().getCurricularCourseToEnrolNameClasses());

	    String degreeName = degreeModuleToEvaluate.getName();

	    if (isAcademicAdminOfficeEmployee() && degreeModuleToEvaluate.getDegreeModule() instanceof CurricularCourse) {
		if (!StringUtils.isEmpty(degreeModuleToEvaluate.getDegreeModule().getCode())) {
		    degreeName = degreeModuleToEvaluate.getDegreeModule().getCode() + " - " + degreeName;
		}

		CurricularCourse curricularCourse = (CurricularCourse) degreeModuleToEvaluate.getDegreeModule();
		degreeName += " (" + getRenderer().studentResources.getString("label.grade.scale") + " - "
			+ curricularCourse.getGradeScaleChain().getDescription() + ") ";
	    }

	    cellName.setBody(new HtmlText(degreeName));

	    // Year
	    final HtmlTableCell yearCell = htmlTableRow.createCell();
	    yearCell.setClasses(getRenderer().getCurricularCourseToEnrolYearClasses());
	    yearCell.setColspan(2);
	    yearCell.setBody(new HtmlText(degreeModuleToEvaluate.getYearFullLabel()));

	    if (!degreeModuleToEvaluate.isOptionalCurricularCourse()) {
		// Ects
		final HtmlTableCell ectsCell = htmlTableRow.createCell();
		ectsCell.setClasses(getRenderer().getCurricularCourseToEnrolEctsClasses());

		final StringBuilder ects = new StringBuilder();
		ects.append(degreeModuleToEvaluate.getEctsCredits()).append(" ")
			.append(getRenderer().studentResources.getString("label.credits.abbreviation"));
		ectsCell.setBody(new HtmlText(ects.toString()));

		HtmlTableCell checkBoxCell = htmlTableRow.createCell();
		checkBoxCell.setClasses(getRenderer().getCurricularCourseToEnrolCheckBoxClasses());

		HtmlCheckBox checkBox = new HtmlCheckBox(false);
		checkBox.setName("degreeModuleToEnrolCheckBox" + degreeModuleToEvaluate.getKey());
		checkBox.setUserValue(degreeModuleToEvaluate.getKey());
		getDegreeModulesToEvaluateController().addCheckBox(checkBox);
		checkBoxCell.setBody(checkBox);
	    } else {
		final HtmlTableCell cell = htmlTableRow.createCell();
		cell.setClasses(getRenderer().getCurricularCourseToEnrolEctsClasses());
		cell.setBody(new HtmlText(""));

		HtmlTableCell linkTableCell = htmlTableRow.createCell();
		linkTableCell.setClasses(getRenderer().getCurricularCourseToEnrolCheckBoxClasses());

		final HtmlActionLink actionLink = new HtmlActionLink();
		actionLink.setText(getRenderer().studentResources.getString("label.chooseOptionalCurricularCourse"));
		actionLink.setController(new OptionalCurricularCourseLinkController(degreeModuleToEvaluate));
		actionLink.setOnClick("$(\\\"form[name='net.sourceforge.fenixedu.presentationTier.formbeans.FenixActionForm']\\\").method.value='prepareChooseOptionalCurricularCourseToEnrol';");
		actionLink.setName("optionalCurricularCourseLink" + degreeModuleToEvaluate.getCurriculumGroup().getIdInternal()
			+ "_" + degreeModuleToEvaluate.getContext().getIdInternal());
		linkTableCell.setBody(actionLink);
	    }

	    if (getRenderer().isEncodeCurricularRules()) {
		encodeCurricularRules(groupTable, degreeModuleToEvaluate);
	    }
	}
    }

    private static class ErasmusExtraCurricularEnrolmentConverter extends Converter {

	@Override
	public Object convert(Class type, Object value) {
	    ArrayList<ErasmusExtraCurricularEnrolmentBean> list = new ArrayList<ErasmusBolonhaStudentEnrollmentBean.ErasmusExtraCurricularEnrolmentBean>();
	    final DomainObjectKeyConverter converter = new DomainObjectKeyConverter();

	    for (String string : (String[]) value) {
		list.add(new ErasmusExtraCurricularEnrolmentBean((CurricularCourse) converter.convert(type, string), true));
	    }

	    return list;
	}

    }

    public CopyCheckBoxValuesController controller = new CopyCheckBoxValuesController();

    @Override
    public HtmlComponent createComponent(Object object, Class type) {
	setBolonhaStudentEnrollmentBean((BolonhaStudentEnrollmentBean) object);

	if (getBolonhaStudentEnrollmentBean() == null) {
	    return new HtmlText();
	}

	final HtmlBlockContainer container = new HtmlBlockContainer();

	HtmlMultipleHiddenField hiddenEnrollments = new HtmlMultipleHiddenField();
	hiddenEnrollments.bind(getRenderer().getInputContext().getMetaObject(), "curriculumModulesToRemove");
	hiddenEnrollments.setConverter(new DomainObjectKeyArrayConverter());
	hiddenEnrollments.setController(getEnrollmentsController());

	HtmlMultipleHiddenField hiddenDegreeModulesToEvaluate = new HtmlMultipleHiddenField();
	hiddenDegreeModulesToEvaluate.bind(getRenderer().getInputContext().getMetaObject(), "degreeModulesToEvaluate");
	hiddenDegreeModulesToEvaluate.setConverter(getBolonhaStudentEnrollmentBean().getDegreeModulesToEvaluateConverter());
	hiddenDegreeModulesToEvaluate.setController(getDegreeModulesToEvaluateController());

	HtmlMultipleHiddenField hiddenExtraCurricularEnrollments = new HtmlMultipleHiddenField();
	hiddenExtraCurricularEnrollments.bind(getRenderer().getInputContext().getMetaObject(), "extraCurricularEnrolments");
	hiddenExtraCurricularEnrollments.setConverter(new ErasmusExtraCurricularEnrolmentConverter());
	hiddenExtraCurricularEnrollments.setController(controller);

	container.addChild(hiddenEnrollments);
	container.addChild(hiddenDegreeModulesToEvaluate);
	container.addChild(hiddenExtraCurricularEnrollments);

	generateGroup(container, getBolonhaStudentEnrollmentBean().getStudentCurricularPlan(), getBolonhaStudentEnrollmentBean()
		.getRootStudentCurriculumGroupBean(), getBolonhaStudentEnrollmentBean().getExecutionPeriod(), 0);

	HtmlTable groupTable = createGroupTable(container, 0);

	HtmlTableRow htmlTableRow = groupTable.createRow();
	htmlTableRow.setClasses(getRenderer().getGroupRowClasses());
	htmlTableRow.createCell().setBody(new HtmlText("Extra Curricular", false));
	HtmlTableCell cell = htmlTableRow.createCell();
	cell.setClasses("aright");

	HtmlCheckBox checkBox = new HtmlCheckBox(false);
	final String name = StringAppender.append("degreeModuleToEnrolCheckBox", "");
	checkBox.setName(name);
	checkBox.setUserValue("true");
	checkBox.setChecked(true);

	cell.setBody(checkBox);
	groupTable = createCoursesTable(container, 0);
	NoCourseGroupCurriculumGroup group = getBolonhaStudentEnrollmentBean().getStudentCurricularPlan()
		.getNoCourseGroupCurriculumGroup(NoCourseGroupCurriculumGroupType.EXTRA_CURRICULAR);
	for (CurricularCourse curricularCourse : ((ErasmusBolonhaStudentEnrollmentBean) getBolonhaStudentEnrollmentBean())
		.getCandidacy().getCurricularCourses()) {
	    if (((ErasmusBolonhaStudentEnrollmentBean) getBolonhaStudentEnrollmentBean()).getCandidacy().getRegistration()
		    .getDegree().hasDegreeCurricularPlans(curricularCourse.getDegreeCurricularPlan())) {
		continue;
	    }

	    if (!(curricularCourse.getExecutionCoursesByExecutionPeriod(
		    ((ErasmusBolonhaStudentEnrollmentBean) getBolonhaStudentEnrollmentBean()).getExecutionPeriod()).size() > 0)) {
		continue;
	    }

	    
	    
	    htmlTableRow = groupTable.createRow();
	    HtmlTableCell cellName = htmlTableRow.createCell();
	    cellName.setClasses(getRenderer().getCurricularCourseToEnrolNameClasses());

	    String degreeName = curricularCourse.getName();

	    if (isAcademicAdminOfficeEmployee() && curricularCourse instanceof CurricularCourse) {
		if (!StringUtils.isEmpty(curricularCourse.getCode())) {
		    degreeName = curricularCourse.getCode() + " - " + degreeName;
		}

		degreeName += " (" + getRenderer().studentResources.getString("label.grade.scale") + " - "
			+ curricularCourse.getGradeScaleChain().getDescription() + ") ";
	    }

	    cellName.setBody(new HtmlText(degreeName));
	    

	    // Year
	    final HtmlTableCell yearCell = htmlTableRow.createCell();
	    yearCell.setClasses(getRenderer().getCurricularCourseToEnrolYearClasses());
	    yearCell.setColspan(2);
	    yearCell.setBody(new HtmlText(getBolonhaStudentEnrollmentBean().getExecutionPeriod().getQualifiedName()));
	    
	    final HtmlTableCell ectsCell = htmlTableRow.createCell();
	    ectsCell.setClasses(getRenderer().getCurricularCourseToEnrolEctsClasses());

	    final StringBuilder ects = new StringBuilder();
	    ects.append(curricularCourse.getEctsCredits()).append(" ")
		    .append(getRenderer().studentResources.getString("label.credits.abbreviation"));
	    ectsCell.setBody(new HtmlText(ects.toString()));

	    HtmlTableCell checkBoxCell = htmlTableRow.createCell();
	    checkBoxCell.setClasses(getRenderer().getCurricularCourseToEnrolCheckBoxClasses());

	    checkBox = new HtmlCheckBox(false);
	    checkBox.setName("extraCurricularEnrolments" + curricularCourse.getClass().getCanonicalName() + ":"
		    + curricularCourse.getIdInternal());
	    checkBox.setUserValue(curricularCourse.getClass().getCanonicalName() + ":" + curricularCourse.getIdInternal());
	    checkBoxCell.setBody(checkBox);
	    controller.addCheckBox(checkBox);
	    
	    if (group.hasEnrolmentWithEnroledState(curricularCourse, ((ErasmusBolonhaStudentEnrollmentBean) getBolonhaStudentEnrollmentBean()).getExecutionPeriod())){
		cellName.setClasses(getRenderer().getEnrolmentNameClasses());
		yearCell.setClasses(getRenderer().getEnrolmentYearClasses());
		ectsCell.setClasses(getRenderer().getEnrolmentEctsClasses());
		checkBoxCell.setClasses(getRenderer().getEnrolmentCheckBoxClasses());
		
		checkBox.setChecked(true);
	    }
	    
	}

	return container;
    }
}
