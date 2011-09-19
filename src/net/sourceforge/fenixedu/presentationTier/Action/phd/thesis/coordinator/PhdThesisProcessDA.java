package net.sourceforge.fenixedu.presentationTier.Action.phd.thesis.coordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.fenixedu.applicationTier.Servico.caseHandling.ExecuteProcessActivity;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.phd.PhdIndividualProgramDocumentType;
import net.sourceforge.fenixedu.domain.phd.PhdProgramDocumentUploadBean;
import net.sourceforge.fenixedu.domain.phd.PhdProgramProcessDocument;
import net.sourceforge.fenixedu.domain.phd.thesis.PhdThesisProcess;
import net.sourceforge.fenixedu.domain.phd.thesis.PhdThesisProcessBean;
import net.sourceforge.fenixedu.domain.phd.thesis.activities.SubmitJuryElementsDocuments;
import net.sourceforge.fenixedu.presentationTier.Action.phd.PhdDocumentsZip;
import net.sourceforge.fenixedu.presentationTier.Action.phd.thesis.CommonPhdThesisProcessDA;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.fenixWebFramework.renderers.components.state.IViewState;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.struts.annotations.Forward;
import pt.ist.fenixWebFramework.struts.annotations.Forwards;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.ist.fenixWebFramework.struts.annotations.Tile;

@Mapping(path = "/phdThesisProcess", module = "coordinator")
@Forwards(tileProperties = @Tile(navLocal = "/coordinator/localNavigationBar.jsp"), value = {

@Forward(name = "submitJuryElementsDocument", path = "/phd/thesis/coordinator/submitJuryElementsDocument.jsp"),

@Forward(name = "manageThesisJuryElements", path = "/phd/thesis/coordinator/manageThesisJuryElements.jsp"),

@Forward(name = "juryReporterFeedbackUpload", path = "/phd/thesis/coordinator/juryReporterFeedbackUpload.jsp"),

@Forward(name = "manageThesisDocuments", path = "/phd/thesis/coordinator/manageThesisDocuments.jsp"),

@Forward(name = "scheduleThesisMeeting", path = "/phd/thesis/coordinator/scheduleThesisMeeting.jsp")

})
public class PhdThesisProcessDA extends CommonPhdThesisProcessDA {

    // Begin thesis jury elements management

    public ActionForward prepareSubmitJuryElementsDocument(ActionMapping mapping, ActionForm actionForm,
	    HttpServletRequest request, HttpServletResponse response) {

	final PhdThesisProcessBean bean = new PhdThesisProcessBean();
	bean.addDocument(new PhdProgramDocumentUploadBean(PhdIndividualProgramDocumentType.JURY_ELEMENTS));
	request.setAttribute("thesisProcessBean", bean);

	return mapping.findForward("submitJuryElementsDocument");
    }

    public ActionForward submitJuryElementsDocumentInvalid(ActionMapping mapping, ActionForm actionForm,
	    HttpServletRequest request, HttpServletResponse response) {
	request.setAttribute("thesisProcessBean", getThesisProcessBean());
	return mapping.findForward("submitJuryElementsDocument");
    }

    public ActionForward submitJuryElementsDocument(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {
	try {

	    final IViewState viewState = RenderUtils.getViewState("thesisProcessBean.edit.documents");
	    if (!viewState.isValid()) {
		return submitJuryElementsDocumentInvalid(mapping, actionForm, request, response);
	    }
	    ExecuteProcessActivity.run(getProcess(request), SubmitJuryElementsDocuments.class, getThesisProcessBean());
	    addSuccessMessage(request, "message.thesis.jury.elements.added.with.success");

	} catch (final DomainException e) {
	    addErrorMessage(request, e.getMessage(), e.getArgs());
	    return submitJuryElementsDocumentInvalid(mapping, actionForm, request, response);
	}

	return viewIndividualProgramProcess(request, getProcess(request));
    }

    public ActionForward manageThesisJuryElements(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {
	return mapping.findForward("manageThesisJuryElements");
    }

    // end of thesis jury elements management

    // Restrict thesis documents

    private static final List<PhdIndividualProgramDocumentType> AVAILABLE_DOCUMENTS_TO_TEACHER = Arrays
	    .asList(new PhdIndividualProgramDocumentType[] { PhdIndividualProgramDocumentType.JURY_ELEMENTS,
		    PhdIndividualProgramDocumentType.JURY_MEETING_MINUTES });

    @Override
    public ActionForward manageThesisDocuments(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {
	List<PhdProgramProcessDocument> sharedDocuments = new ArrayList<PhdProgramProcessDocument>();

	PhdThesisProcess thesisProcess = getProcess(request);
	Set<PhdProgramProcessDocument> latestDocumentVersions = thesisProcess.getLatestDocumentVersions();
	for (PhdProgramProcessDocument phdProgramProcessDocument : latestDocumentVersions) {
	    if (AVAILABLE_DOCUMENTS_TO_TEACHER.contains(phdProgramProcessDocument.getDocumentType())) {
		sharedDocuments.add(phdProgramProcessDocument);
	    }
	}

	request.setAttribute("sharedDocuments", sharedDocuments);

	return super.manageThesisDocuments(mapping, actionForm, request, response);
    }

    @Override
    public ActionForward downloadThesisDocuments(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) throws IOException {

	writeFile(response, getThesisDocumentsFilename(request), PhdDocumentsZip.ZIP_MIME_TYPE, createThesisZip(request));
	return null;
    }

    protected byte[] createThesisZip(HttpServletRequest request) throws IOException {
	PhdThesisProcess thesisProcess = getProcess(request);
	List<PhdProgramProcessDocument> sharedDocuments = new ArrayList<PhdProgramProcessDocument>();
	Set<PhdProgramProcessDocument> latestDocumentVersions = thesisProcess.getLatestDocumentVersions();

	for (PhdProgramProcessDocument phdProgramProcessDocument : latestDocumentVersions) {
	    if (AVAILABLE_DOCUMENTS_TO_TEACHER.contains(phdProgramProcessDocument.getDocumentType())) {
		sharedDocuments.add(phdProgramProcessDocument);
	    }
	}

	return PhdDocumentsZip.zip(sharedDocuments);
    }

    // End of restrict thesis documents

}
