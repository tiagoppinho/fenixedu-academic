/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.fenixedu.domain;

import static net.sourceforge.fenixedu.injectionCode.AccessControl.check;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.fenixedu.applicationTier.Servico.teacher.professorship.ResponsibleForValidator;
import net.sourceforge.fenixedu.applicationTier.Servico.teacher.professorship.ResponsibleForValidator.InvalidCategory;
import net.sourceforge.fenixedu.applicationTier.Servico.teacher.professorship.ResponsibleForValidator.MaxResponsibleForExceed;
import net.sourceforge.fenixedu.dataTransferObject.InfoPersonEditor;
import net.sourceforge.fenixedu.dataTransferObject.externalServices.PersonInformationFromUniqueCardDTO;
import net.sourceforge.fenixedu.dataTransferObject.person.PersonBean;
import net.sourceforge.fenixedu.domain.accounting.AcademicEvent;
import net.sourceforge.fenixedu.domain.accounting.AccountingTransaction;
import net.sourceforge.fenixedu.domain.accounting.Entry;
import net.sourceforge.fenixedu.domain.accounting.Event;
import net.sourceforge.fenixedu.domain.accounting.EventType;
import net.sourceforge.fenixedu.domain.accounting.PaymentCode;
import net.sourceforge.fenixedu.domain.accounting.PaymentCodeType;
import net.sourceforge.fenixedu.domain.accounting.Receipt;
import net.sourceforge.fenixedu.domain.accounting.ResidenceEvent;
import net.sourceforge.fenixedu.domain.accounting.ServiceAgreement;
import net.sourceforge.fenixedu.domain.accounting.ServiceAgreementTemplate;
import net.sourceforge.fenixedu.domain.accounting.events.AdministrativeOfficeFeeAndInsuranceEvent;
import net.sourceforge.fenixedu.domain.accounting.events.AnnualEvent;
import net.sourceforge.fenixedu.domain.accounting.events.PastAdministrativeOfficeFeeAndInsuranceEvent;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.GratuityEvent;
import net.sourceforge.fenixedu.domain.accounting.events.insurance.InsuranceEvent;
import net.sourceforge.fenixedu.domain.administrativeOffice.AdministrativeOffice;
import net.sourceforge.fenixedu.domain.candidacy.Candidacy;
import net.sourceforge.fenixedu.domain.candidacy.CandidacySituationType;
import net.sourceforge.fenixedu.domain.candidacy.DFACandidacy;
import net.sourceforge.fenixedu.domain.candidacy.DegreeCandidacy;
import net.sourceforge.fenixedu.domain.candidacy.StudentCandidacy;
import net.sourceforge.fenixedu.domain.candidacyProcess.IndividualCandidacy;
import net.sourceforge.fenixedu.domain.candidacyProcess.IndividualCandidacyPersonalDetails;
import net.sourceforge.fenixedu.domain.candidacyProcess.graduatedPerson.DegreeCandidacyForGraduatedPerson;
import net.sourceforge.fenixedu.domain.candidacyProcess.over23.Over23IndividualCandidacy;
import net.sourceforge.fenixedu.domain.candidacyProcess.secondCycle.SecondCycleIndividualCandidacy;
import net.sourceforge.fenixedu.domain.candidacyProcess.standalone.StandaloneIndividualCandidacy;
import net.sourceforge.fenixedu.domain.contacts.EmailAddress;
import net.sourceforge.fenixedu.domain.contacts.MobilePhone;
import net.sourceforge.fenixedu.domain.contacts.PartyContact;
import net.sourceforge.fenixedu.domain.contacts.PartyContactType;
import net.sourceforge.fenixedu.domain.contacts.Phone;
import net.sourceforge.fenixedu.domain.contacts.PhysicalAddress;
import net.sourceforge.fenixedu.domain.contacts.PhysicalAddressData;
import net.sourceforge.fenixedu.domain.contacts.WebAddress;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.documents.AnnualIRSDeclarationDocument;
import net.sourceforge.fenixedu.domain.documents.GeneratedDocument;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.messaging.Forum;
import net.sourceforge.fenixedu.domain.messaging.ForumSubscription;
import net.sourceforge.fenixedu.domain.organizationalStructure.Accountability;
import net.sourceforge.fenixedu.domain.organizationalStructure.AccountabilityType;
import net.sourceforge.fenixedu.domain.organizationalStructure.AccountabilityTypeEnum;
import net.sourceforge.fenixedu.domain.organizationalStructure.EmployeeContract;
import net.sourceforge.fenixedu.domain.organizationalStructure.ExternalContract;
import net.sourceforge.fenixedu.domain.organizationalStructure.Function;
import net.sourceforge.fenixedu.domain.organizationalStructure.FunctionType;
import net.sourceforge.fenixedu.domain.organizationalStructure.Invitation;
import net.sourceforge.fenixedu.domain.organizationalStructure.Party;
import net.sourceforge.fenixedu.domain.organizationalStructure.PersonFunction;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchContract;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.person.Gender;
import net.sourceforge.fenixedu.domain.person.HumanName;
import net.sourceforge.fenixedu.domain.person.IDDocumentType;
import net.sourceforge.fenixedu.domain.person.IdDocument;
import net.sourceforge.fenixedu.domain.person.IdDocumentTypeObject;
import net.sourceforge.fenixedu.domain.person.MaritalStatus;
import net.sourceforge.fenixedu.domain.person.PersonName;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.personnelSection.contracts.PersonProfessionalData;
import net.sourceforge.fenixedu.domain.phd.alert.PhdAlertMessage;
import net.sourceforge.fenixedu.domain.phd.candidacy.PHDProgramCandidacy;
import net.sourceforge.fenixedu.domain.student.Registration;
import net.sourceforge.fenixedu.domain.student.RegistrationProtocol;
import net.sourceforge.fenixedu.domain.teacher.Career;
import net.sourceforge.fenixedu.domain.teacher.ProfessionalCareer;
import net.sourceforge.fenixedu.domain.teacher.TeachingCareer;
import net.sourceforge.fenixedu.domain.thesis.Thesis;
import net.sourceforge.fenixedu.domain.thesis.ThesisEvaluationParticipant;
import net.sourceforge.fenixedu.domain.thesis.ThesisParticipationType;
import net.sourceforge.fenixedu.domain.vigilancy.ExamCoordinator;
import net.sourceforge.fenixedu.domain.vigilancy.UnavailablePeriod;
import net.sourceforge.fenixedu.domain.vigilancy.Vigilancy;
import net.sourceforge.fenixedu.domain.vigilancy.VigilantGroup;
import net.sourceforge.fenixedu.domain.vigilancy.VigilantWrapper;
import net.sourceforge.fenixedu.injectionCode.AccessControl;
import net.sourceforge.fenixedu.predicates.AcademicPredicates;
import net.sourceforge.fenixedu.predicates.RolePredicates;
import net.sourceforge.fenixedu.util.Bundle;
import net.sourceforge.fenixedu.util.Money;
import net.sourceforge.fenixedu.util.PeriodState;
import net.sourceforge.fenixedu.util.StringFormatter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.i18n.LocalizedString.Builder;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.YearMonthDay;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

public class Person extends Person_Base {

    private static final Integer MAX_VALIDATION_REQUESTS = 5;
    public static com.google.common.base.Function<User, Person> userToPerson =
            new com.google.common.base.Function<User, Person>() {
                @Override
                public Person apply(User user) {
                    return user.getPerson();
                }
            };

    public static com.google.common.base.Function<Person, User> personToUser =
            new com.google.common.base.Function<Person, User>() {
                @Override
                public User apply(Person person) {
                    return person.getUser();
                }
            };

    /***************************************************************************
     * BUSINESS SERVICES *
     **************************************************************************/

    private IdDocument getIdDocument() {
        final Iterator<IdDocument> documentIterator = getIdDocumentsSet().iterator();
        return documentIterator.hasNext() ? documentIterator.next() : null;
    }

    @Override
    public void setUser(User user) {
        super.setUser(user);
        if (getProfile() != null) {
            getProfile().setAvatarUrl(
                    CoreConfiguration.getConfiguration().applicationUrl() + "/user/photo/" + getUser().getUsername());
        }
    }

    @Override
    public MultiLanguageString getPartyName() {
        Builder builder = new LocalizedString.Builder();
        for (Locale locale : CoreConfiguration.supportedLocales()) {
            builder.with(locale, getName());
        }
        return MultiLanguageString.fromLocalizedString(builder.build());
    }

    @Override
    public void setPartyName(final MultiLanguageString partyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        if (getProfile() != null) {
            return getProfile().getFullName();
        }
        if (super.getPartyName() != null) {
            return super.getPartyName().getPreferedContent();
        }
        return null;
    }

    @Override
    public void setName(final String name) {
        ensureUserProfile();
        if (getProfile().getGivenNames() == null && getProfile().getFamilyNames() == null) {
            HumanName split = HumanName.decompose(name, false);
            getProfile().changeName(split.getGivenNames(), split.getFamilyNames(), getProfile().getDisplayName());
        } else {
            throw new Error("Could not edit person name using unseparated name input");
        }
    }

    /**
     * @deprecated Use {@link UserProfile#getGivenNames()}
     */
    @Deprecated
    @Override
    public String getGivenNames() {
        if (getProfile() != null) {
            return getProfile().getGivenNames();
        }
        if (super.getGivenNames() != null) {
            super.getGivenNames();
        }
        return HumanName.decompose(getName(), false).getGivenNames();
    }

    /**
     * @deprecated Use {@link UserProfile#changeName(String, String, String)}
     */
    @Override
    @Deprecated
    public void setGivenNames(final String name) {
        ensureUserProfile();
        getProfile().changeName(name, getProfile().getFamilyNames(), getProfile().getDisplayName());
    }

    /**
     * @deprecated Use {@link UserProfile#getFamilyNames()}
     */
    @Deprecated
    @Override
    public String getFamilyNames() {
        if (getProfile() != null) {
            return getProfile().getFamilyNames();
        }
        if (super.getFamilyNames() != null) {
            super.getFamilyNames();
        }
        return HumanName.decompose(getName(), false).getFamilyNames();
    }

    /**
     * @deprecated Use {@link UserProfile#changeName(String, String, String)}
     */
    @Deprecated
    @Override
    public void setFamilyNames(final String name) {
        ensureUserProfile();
        getProfile().changeName(getProfile().getGivenNames(), name, getProfile().getDisplayName());
    }

    @Deprecated
    public void setNames(final String name, final String givenNames, final String familyNames) {
        ensureUserProfile();
        if (givenNames != null || familyNames != null) {
            getProfile().changeName(givenNames, familyNames, getProfile().getDisplayName());
        } else {
            HumanName split = HumanName.decompose(name, false);
            getProfile().changeName(split.getGivenNames(), split.getFamilyNames(), getProfile().getDisplayName());
        }
    }

    @Override
    public void setDocumentIdNumber(final String documentIdNumber) {
        if (documentIdNumber == null || StringUtils.isEmpty(documentIdNumber.trim())) {
            throw new DomainException("error.person.empty.documentIdNumber");
        }
        IdDocument idDocument = getIdDocument();
        if (idDocument == null) {
            idDocument = new IdDocument(this, documentIdNumber, (IdDocumentTypeObject) null);
        } else {
            idDocument.setValue(documentIdNumber);
        }
        logSetterNullString("log.personInformation.edit.generalTemplate.personalId", getDocumentIdNumber(), documentIdNumber,
                "label.documentNumber");
        super.setDocumentIdNumber(documentIdNumber);
    }

    @Override
    public void setIdDocumentType(final IDDocumentType idDocumentType) {
        if (idDocumentType == null) {
            throw new DomainException("error.person.empty.idDocumentType");
        }
        IdDocument idDocument = getIdDocument();
        if (idDocument == null) {
            idDocument = new IdDocument(this, null, idDocumentType);
        } else {
            idDocument.setIdDocumentType(idDocumentType);
        }

        logSetterNullEnum("log.personInformation.edit.generalTemplate.personalId", getIdDocumentType(), idDocumentType,
                "label.documentIdType");
        super.setIdDocumentType(idDocumentType);
    }

    public void setIdentification(String documentIdNumber, final IDDocumentType idDocumentType) {
        documentIdNumber = StringUtils.trimToNull(documentIdNumber);
        if (documentIdNumber != null && idDocumentType != null
                && checkIfDocumentNumberIdAndDocumentIdTypeExists(documentIdNumber, idDocumentType)) {
            throw new DomainException("error.person.existent.docIdAndType");
        }
        setDocumentIdNumber(documentIdNumber);
        setIdDocumentType(idDocumentType);
    }

    public void setIdentificationAndNames(String documentIdNumber, final IDDocumentType idDocumentType, final String name,
            final String givenNames, final String familyNames) {
        setNames(name, givenNames, familyNames);
        setIdentification(documentIdNumber, idDocumentType);
    }

    private boolean checkIfDocumentNumberIdAndDocumentIdTypeExists(final String documentIDNumber,
            final IDDocumentType documentType) {
        final Person person = readByDocumentIdNumberAndIdDocumentType(documentIDNumber, documentType);
        return person != null && !person.equals(this);
    }

    final public String getValidatedName() {
        return StringFormatter.prettyPrint(getName());
    }

    private Person() {
        super();
        setMaritalStatus(MaritalStatus.UNKNOWN);
        createUser();
    }

    public Person(User user) {
        super();
        setMaritalStatus(MaritalStatus.UNKNOWN);
        setUser(user);
    }

    public void createUser() {
        if (getUser() == null) {
            setUser(new User(getProfile()));
        } else {
            throw new DomainException("error.person.already.has.user");
        }
    }

    public Person(final String name, final String identificationDocumentNumber, final IDDocumentType identificationDocumentType,
            final Gender gender) {

        this();
        setName(name);
        setGender(gender);
        setMaritalStatus(MaritalStatus.SINGLE);
        setIdentification(identificationDocumentNumber, identificationDocumentType);
    }

    public Person(final PersonBean personBean) {
        this(personBean, false, false);
    }

    public Person(final PersonBean personBean, final boolean validateEmail, final boolean validateAddress) {
        super();

        setProperties(personBean);

        final PhysicalAddress physicalAddress =
                PhysicalAddress.createPhysicalAddress(this, personBean.getPhysicalAddressData(), PartyContactType.PERSONAL, true);
        if (validateAddress) {
            physicalAddress.setValid();
        }
        Phone.createPhone(this, personBean.getPhone(), PartyContactType.PERSONAL, true);
        MobilePhone.createMobilePhone(this, personBean.getMobile(), PartyContactType.PERSONAL, true);
        final EmailAddress emailAddress =
                EmailAddress.createEmailAddress(this, personBean.getEmail(), PartyContactType.PERSONAL, true);
        if (validateEmail) {
            emailAddress.setValid();
        }
        WebAddress.createWebAddress(this, personBean.getWebAddress(), PartyContactType.PERSONAL, true);
    }

    public Person(final IndividualCandidacyPersonalDetails candidacyPersonalDetails) {
        this();

        this.setCountry(candidacyPersonalDetails.getCountry());
        this.setDateOfBirthYearMonthDay(candidacyPersonalDetails.getDateOfBirthYearMonthDay());
        this.setDocumentIdNumber(candidacyPersonalDetails.getDocumentIdNumber());
        this.setExpirationDateOfDocumentIdYearMonthDay(candidacyPersonalDetails.getExpirationDateOfDocumentIdYearMonthDay());
        this.setGender(candidacyPersonalDetails.getGender());
        this.setIdDocumentType(candidacyPersonalDetails.getIdDocumentType());
        this.setGivenNames(candidacyPersonalDetails.getGivenNames());
        this.setFamilyNames(candidacyPersonalDetails.getFamilyNames());
        this.setSocialSecurityNumber(candidacyPersonalDetails.getSocialSecurityNumber());

        final PhysicalAddressData physicalAddressData =
                new PhysicalAddressData(candidacyPersonalDetails.getAddress(), candidacyPersonalDetails.getAreaCode(), "",
                        candidacyPersonalDetails.getArea(), "", "", "", candidacyPersonalDetails.getCountryOfResidence());
        PhysicalAddress.createPhysicalAddress(this, physicalAddressData, PartyContactType.PERSONAL, true);
        Phone.createPhone(this, candidacyPersonalDetails.getTelephoneContact(), PartyContactType.PERSONAL, true);
        EmailAddress.createEmailAddress(this, candidacyPersonalDetails.getEmail(), PartyContactType.PERSONAL, true);
    }

    private Person(final String name, final Gender gender, final PhysicalAddressData data, final String phone,
            final String mobile, final String homepage, final String email, final String documentIDNumber,
            final IDDocumentType documentType) {

        this();

        setName(name);
        setGender(gender);
        setIdentification(documentIDNumber, documentType);

        PhysicalAddress.createPhysicalAddress(this, data, PartyContactType.PERSONAL, true);
        Phone.createPhone(this, phone, PartyContactType.PERSONAL, true);
        MobilePhone.createMobilePhone(this, mobile, PartyContactType.PERSONAL, true);
        EmailAddress.createEmailAddress(this, email, PartyContactType.PERSONAL, true);
        WebAddress.createWebAddress(this, homepage, PartyContactType.PERSONAL, true);
    }

    static public Person createExternalPerson(final String name, final Gender gender, final PhysicalAddressData data,
            final String phone, final String mobile, final String homepage, final String email, final String documentIdNumber,
            final IDDocumentType documentType) {
        return new Person(name, gender, data, phone, mobile, homepage, email, documentIdNumber, documentType);
    }

    public Person(final String name, final Gender gender, final String documentIDNumber, final IDDocumentType documentType) {

        this();
        setName(name);
        setGender(gender);
        setIdentification(documentIDNumber, documentType);
    }

    public Person edit(final PersonBean personBean) {
        check(this, RolePredicates.MANAGER_OR_ACADEMIC_ADMINISTRATIVE_OFFICE_PREDICATE);
        setProperties(personBean);
        setDefaultPhysicalAddressData(personBean.getPhysicalAddressData(), true);
        setDefaultPhoneNumber(personBean.getPhone());
        setDefaultMobilePhoneNumber(personBean.getMobile());
        setDefaultWebAddressUrl(personBean.getWebAddress());
        setDefaultEmailAddressValue(personBean.getEmail(), true);
        return this;
    }

    public Person editPersonalInformation(final PersonBean personBean) {
        check(this, AcademicPredicates.EDIT_STUDENT_PERSONAL_DATA);
        setProperties(personBean);
        return this;
    }

    public Person editByPublicCandidate(final PersonBean personBean) {
        setNames(personBean.getName(), personBean.getGivenNames(), personBean.getFamilyNames());
        setGender(personBean.getGender());
        setIdentification(personBean.getDocumentIdNumber(), personBean.getIdDocumentType());
        setExpirationDateOfDocumentIdYearMonthDay(personBean.getDocumentIdExpirationDate());
        setSocialSecurityNumber(personBean.getSocialSecurityNumber());
        setDateOfBirthYearMonthDay(personBean.getDateOfBirth());
        setCountry(personBean.getNationality());
        setDefaultPhysicalAddressData(personBean.getPhysicalAddressData());
        setDefaultPhoneNumber(personBean.getPhone());
        setDefaultEmailAddressValue(personBean.getEmail(), true);
        return this;
    }

    public Person edit(final IndividualCandidacyPersonalDetails candidacyExternalDetails) {
        this.setCountry(candidacyExternalDetails.getCountry());

        this.setDateOfBirthYearMonthDay(candidacyExternalDetails.getDateOfBirthYearMonthDay());
        this.setIdentification(candidacyExternalDetails.getDocumentIdNumber(), candidacyExternalDetails.getIdDocumentType());
        this.setExpirationDateOfDocumentIdYearMonthDay(candidacyExternalDetails.getExpirationDateOfDocumentIdYearMonthDay());
        this.setGender(candidacyExternalDetails.getGender());
        this.setGivenNames(candidacyExternalDetails.getGivenNames());
        this.setFamilyNames(candidacyExternalDetails.getFamilyNames());
        this.setSocialSecurityNumber(candidacyExternalDetails.getSocialSecurityNumber());

        final PhysicalAddressData physicalAddressData =
                new PhysicalAddressData(candidacyExternalDetails.getAddress(), candidacyExternalDetails.getAreaCode(),
                        getAreaOfAreaCode(), candidacyExternalDetails.getArea(), getParishOfResidence(),
                        getDistrictSubdivisionOfResidence(), getDistrictOfResidence(),
                        candidacyExternalDetails.getCountryOfResidence());
        setDefaultPhysicalAddressData(physicalAddressData);
        setDefaultPhoneNumber(candidacyExternalDetails.getTelephoneContact());
        setDefaultEmailAddressValue(candidacyExternalDetails.getEmail());

        return this;
    }

    /**
     * FIXME: remove on the next major version
     */
    @Deprecated
    public void editFromBean(final PersonInformationFromUniqueCardDTO personDTO) throws ParseException {
        personDTO.edit(this);
    }

    public void edit(final String name, final String address, final String phone, final String mobile, final String homepage,
            final String email) {
        setName(name);
        setAddress(address);
        setDefaultPhoneNumber(phone);
        setDefaultMobilePhoneNumber(mobile);
        setDefaultEmailAddressValue(email);
        setDefaultWebAddressUrl(homepage);
    }

    public void editPersonalData(final String documentIdNumber, final IDDocumentType documentType, final String personName,
            final String socialSecurityNumber) {
        setName(personName);
        setIdentification(documentIdNumber, documentType);
        setSocialSecurityNumber(socialSecurityNumber);
    }

    public void editPersonWithExternalData(final PersonBean personBean) {
        editPersonWithExternalData(personBean, false);
    }

    public Person editPersonWithExternalData(final PersonBean personBean, final boolean updateExistingContacts) {

        setProperties(personBean);
        setDefaultPhysicalAddressData(personBean.getPhysicalAddressData());

        if (updateExistingContacts) {
            setDefaultPhoneNumber(personBean.getPhone());
            setDefaultMobilePhoneNumber(personBean.getMobile());
            setDefaultWebAddressUrl(personBean.getWebAddress());
            setDefaultEmailAddressValue(personBean.getEmail());
        } else {
            Phone.createPhone(this, personBean.getPhone(), PartyContactType.PERSONAL, !hasDefaultPhone());
            MobilePhone.createMobilePhone(this, personBean.getMobile(), PartyContactType.PERSONAL, !hasDefaultMobilePhone());
            EmailAddress.createEmailAddress(this, personBean.getEmail(), PartyContactType.PERSONAL, !hasDefaultEmailAddress());
            WebAddress.createWebAddress(this, personBean.getWebAddress(), PartyContactType.PERSONAL, !hasDefaultWebAddress());
        }

        return this;
    }

    @Deprecated
    public void update(final InfoPersonEditor updatedPersonalData, final Country country) {
        updateProperties(updatedPersonalData);
        if (country != null) {
            setCountry(country);
        }
    }

    /**
     * 
     * @deprecated use edit(PersonBean personBean)
     * @see edit(PersonBean personBean)
     */
    @Deprecated
    public void edit(final InfoPersonEditor personToEdit, final Country country) {
        setProperties(personToEdit);
        if (country != null) {
            setCountry(country);
        }
    }

    public String getUsername() {
        User user = getUser();
        return user == null ? null : user.getUsername();
    }

    public Boolean hasRole(final RoleType roleType) {
        return roleType.isMember(getUser());
    }

    public Registration getStudentByType(final DegreeType degreeType) {
        for (final Registration registration : this.getStudents()) {
            if (registration.getDegreeType() == degreeType) {
                return registration;
            }
        }
        return null;
    }

    public Boolean getIsExamCoordinatorInCurrentYear() {
        final ExamCoordinator examCoordinator =
                this.getExamCoordinatorForGivenExecutionYear(ExecutionYear.readCurrentExecutionYear());
        return examCoordinator == null ? false : true;
    }

    public List<VigilantGroup> getVisibleVigilantGroups(final ExecutionYear executionYear) {

        final Set<VigilantGroup> groups = new HashSet<VigilantGroup>();

        final Employee employee = this.getEmployee();
        if (employee != null) {
            final Department department =
                    employee.getLastDepartmentWorkingPlace(executionYear.getBeginDateYearMonthDay(),
                            executionYear.getEndDateYearMonthDay());
            if (department != null) {
                groups.addAll(department.getVigilantGroupsForGivenExecutionYear(executionYear));
            }
        } else {
            for (final VigilantWrapper vigilantWrapper : this.getVigilantWrapperForExecutionYear(executionYear)) {
                groups.add(vigilantWrapper.getVigilantGroup());
            }
        }

        return new ArrayList<VigilantGroup>(groups);
    }

    public List<VigilantWrapper> getVigilantWrapperForExecutionYear(final ExecutionYear executionYear) {
        final List<VigilantWrapper> wrappers = new ArrayList<VigilantWrapper>();
        for (final VigilantWrapper wrapper : getVigilantWrappersSet()) {

            if (wrapper.getExecutionYear() == executionYear) {
                wrappers.add(wrapper);
            }
        }

        return wrappers;
    }

    public List<VigilantGroup> getVigilantGroupsForExecutionYear(final ExecutionYear executionYear) {
        final List<VigilantGroup> groups = new ArrayList<VigilantGroup>();
        for (final VigilantWrapper wrapper : getVigilantWrappersSet()) {
            final VigilantGroup group = wrapper.getVigilantGroup();
            if (group.getExecutionYear().equals(executionYear)) {
                groups.add(group);
            }
        }
        return groups;
    }

    public boolean isAllowedToSpecifyUnavailablePeriod() {
        final DateTime currentDate = new DateTime();
        final List<VigilantGroup> groupsForYear = getVigilantGroupsForExecutionYear(ExecutionYear.readCurrentExecutionYear());
        for (final VigilantGroup group : groupsForYear) {
            if (group.canSpecifyUnavailablePeriodIn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public List<Vigilancy> getVigilanciesForYear(final ExecutionYear executionYear) {
        final List<Vigilancy> vigilancies = new ArrayList<Vigilancy>();
        for (final VigilantWrapper vigilantWrapper : this.getVigilantWrappersSet()) {
            if (vigilantWrapper.getExecutionYear().equals(executionYear)) {
                vigilancies.addAll(vigilantWrapper.getVigilanciesSet());
            }
        }
        return vigilancies;
    }

    @Atomic
    public void addExamCoordinator(final ExecutionYear executionYear, final Unit unit) {
        RoleType.grant(RoleType.EXAM_COORDINATOR, this.getUser());
        new ExamCoordinator(this, executionYear, unit);
    }

    public ExamCoordinator getExamCoordinatorForGivenExecutionYear(final ExecutionYear executionYear) {
        final Collection<ExamCoordinator> examCoordinators = this.getExamCoordinatorsSet();
        for (final ExamCoordinator examCoordinator : examCoordinators) {
            if (examCoordinator.getExecutionYear().equals(executionYear)) {
                return examCoordinator;
            }
        }
        return null;
    }

    public Boolean isExamCoordinatorForVigilantGroup(final VigilantGroup group) {
        final ExamCoordinator coordinator = getExamCoordinatorForGivenExecutionYear(group.getExecutionYear());
        return coordinator == null ? Boolean.FALSE : group.getExamCoordinatorsSet().contains(coordinator);
    }

    public ExamCoordinator getCurrentExamCoordinator() {
        return getExamCoordinatorForGivenExecutionYear(ExecutionYear.readCurrentExecutionYear());
    }

    public double getVigilancyPointsForGivenYear(final ExecutionYear executionYear) {
        final List<VigilantWrapper> vigilants = this.getVigilantWrapperForExecutionYear(executionYear);
        if (vigilants.isEmpty()) {
            return 0;
        } else {
            double points = 0;
            for (final VigilantWrapper vigilant : vigilants) {
                points += vigilant.getPoints();
            }
            return points;
        }
    }

    public double getTotalVigilancyPoints() {
        final Collection<VigilantWrapper> vigilants = this.getVigilantWrappersSet();

        double points = 0;
        for (final VigilantWrapper vigilant : vigilants) {
            points += vigilant.getPoints();
        }
        return points;
    }

    /***************************************************************************
     * PRIVATE METHODS *
     **************************************************************************/

    private void setProperties(final InfoPersonEditor infoPerson) {

        setName(infoPerson.getNome());
        setIdentification(infoPerson.getNumeroDocumentoIdentificacao(), infoPerson.getTipoDocumentoIdentificacao());
        setFiscalCode(infoPerson.getCodigoFiscal());

        setDefaultPhysicalAddressData(infoPerson.getPhysicalAddressData());
        setDefaultWebAddressUrl(infoPerson.getEnderecoWeb());
        setDefaultPhoneNumber(infoPerson.getTelefone());
        setDefaultMobilePhoneNumber(infoPerson.getTelemovel());
        setDefaultEmailAddressValue(infoPerson.getEmail());

        setWorkPhoneNumber(infoPerson.getWorkPhone());

        setDistrictSubdivisionOfBirth(infoPerson.getConcelhoNaturalidade());
        if (infoPerson.getDataEmissaoDocumentoIdentificacao() != null) {
            setEmissionDateOfDocumentIdYearMonthDay(YearMonthDay
                    .fromDateFields(infoPerson.getDataEmissaoDocumentoIdentificacao()));
        }
        if (infoPerson.getDataValidadeDocumentoIdentificacao() != null) {
            setExpirationDateOfDocumentIdYearMonthDay(YearMonthDay.fromDateFields(infoPerson
                    .getDataValidadeDocumentoIdentificacao()));
        }
        setDistrictOfBirth(infoPerson.getDistritoNaturalidade());

        setMaritalStatus(infoPerson.getMaritalStatus());
        setParishOfBirth(infoPerson.getFreguesiaNaturalidade());
        setEmissionLocationOfDocumentId(infoPerson.getLocalEmissaoDocumentoIdentificacao());

        if (infoPerson.getNascimento() != null) {
            setDateOfBirthYearMonthDay(YearMonthDay.fromDateFields(infoPerson.getNascimento()));
        }
        setNameOfMother(infoPerson.getNomeMae());
        setNameOfFather(infoPerson.getNomePai());
        setSocialSecurityNumber(infoPerson.getNumContribuinte());

        setProfession(infoPerson.getProfissao());
        setGender(infoPerson.getSexo());

        setAvailableEmail(infoPerson.getAvailableEmail() != null ? infoPerson.getAvailableEmail() : Boolean.TRUE);
        setAvailableWebSite(infoPerson.getAvailableWebSite() != null ? infoPerson.getAvailableWebSite() : Boolean.TRUE);
    }

    private void updateProperties(final InfoPersonEditor infoPerson) {
        setName(valueToUpdateIfNewNotNull(getName(), infoPerson.getNome()));
        setIdentification(valueToUpdateIfNewNotNull(getDocumentIdNumber(), infoPerson.getNumeroDocumentoIdentificacao()),
                (IDDocumentType) valueToUpdateIfNewNotNull(getIdDocumentType(), infoPerson.getTipoDocumentoIdentificacao()));

        setFiscalCode(valueToUpdateIfNewNotNull(getFiscalCode(), infoPerson.getCodigoFiscal()));

        setEmissionDateOfDocumentIdYearMonthDay(infoPerson.getDataEmissaoDocumentoIdentificacao() != null ? YearMonthDay
                .fromDateFields(infoPerson.getDataEmissaoDocumentoIdentificacao()) : getEmissionDateOfDocumentIdYearMonthDay());
        setEmissionLocationOfDocumentId(valueToUpdateIfNewNotNull(getEmissionLocationOfDocumentId(),
                infoPerson.getLocalEmissaoDocumentoIdentificacao()));
        setExpirationDateOfDocumentIdYearMonthDay(infoPerson.getDataValidadeDocumentoIdentificacao() != null ? YearMonthDay
                .fromDateFields(infoPerson.getDataValidadeDocumentoIdentificacao()) : getExpirationDateOfDocumentIdYearMonthDay());

        final MaritalStatus maritalStatus =
                (MaritalStatus) valueToUpdateIfNewNotNull(getMaritalStatus(), infoPerson.getMaritalStatus());
        setMaritalStatus(maritalStatus);

        setDateOfBirthYearMonthDay(infoPerson.getNascimento() != null ? YearMonthDay.fromDateFields(infoPerson.getNascimento()) : getDateOfBirthYearMonthDay());
        setParishOfBirth(valueToUpdateIfNewNotNull(getParishOfBirth(), infoPerson.getFreguesiaNaturalidade()));
        setDistrictSubdivisionOfBirth(valueToUpdateIfNewNotNull(getDistrictSubdivisionOfBirth(),
                infoPerson.getConcelhoNaturalidade()));
        setDistrictOfBirth(valueToUpdateIfNewNotNull(getDistrictOfBirth(), infoPerson.getDistritoNaturalidade()));

        setNameOfMother(valueToUpdateIfNewNotNull(getNameOfMother(), infoPerson.getNomeMae()));
        setNameOfFather(valueToUpdateIfNewNotNull(getNameOfFather(), infoPerson.getNomePai()));
        setSocialSecurityNumber(valueToUpdateIfNewNotNull(getSocialSecurityNumber(), infoPerson.getNumContribuinte()));
        setProfession(valueToUpdateIfNewNotNull(getProfession(), infoPerson.getProfissao()));
        setGender((Gender) valueToUpdateIfNewNotNull(getGender(), infoPerson.getSexo()));

        final PhysicalAddressData data = new PhysicalAddressData();
        data.setAddress(valueToUpdateIfNewNotNull(getAddress(), infoPerson.getMorada()));
        data.setAreaCode(valueToUpdateIfNewNotNull(getAreaCode(), infoPerson.getCodigoPostal()));
        data.setAreaOfAreaCode(valueToUpdateIfNewNotNull(getAreaOfAreaCode(), infoPerson.getLocalidadeCodigoPostal()));
        data.setArea(valueToUpdateIfNewNotNull(getArea(), infoPerson.getLocalidade()));
        data.setParishOfResidence(valueToUpdateIfNewNotNull(getParishOfResidence(), infoPerson.getFreguesiaMorada()));
        data.setDistrictSubdivisionOfResidence(valueToUpdateIfNewNotNull(getDistrictSubdivisionOfResidence(),
                infoPerson.getConcelhoMorada()));
        data.setDistrictOfResidence(valueToUpdateIfNewNotNull(getDistrictOfResidence(), infoPerson.getDistritoMorada()));
        data.setCountryOfResidence(getCountryOfResidence());
        setDefaultPhysicalAddressData(data);

        if (!hasAnyPartyContact(Phone.class)) {
            Phone.createPhone(this, infoPerson.getTelefone(), PartyContactType.PERSONAL, true);
            Phone.createPhone(this, infoPerson.getWorkPhone(), PartyContactType.WORK, true);
        }
        if (!hasAnyPartyContact(MobilePhone.class)) {
            MobilePhone.createMobilePhone(this, infoPerson.getTelemovel(), PartyContactType.PERSONAL, false);
        }
        if (!hasAnyPartyContact(EmailAddress.class) && EmailValidator.getInstance().isValid(infoPerson.getEmail())) {
            EmailAddress.createEmailAddress(this, infoPerson.getEmail(), PartyContactType.PERSONAL, false);
        }
        if (!hasAnyPartyContact(WebAddress.class) && !StringUtils.isEmpty(infoPerson.getEnderecoWeb())) {
            WebAddress.createWebAddress(this, infoPerson.getEnderecoWeb(), PartyContactType.PERSONAL, false);
        }
    }

    private String valueToUpdateIfNewNotNull(final String actualValue, final String newValue) {

        if (newValue == null || newValue.length() == 0) {
            return actualValue;
        }
        return newValue;

    }

    private Object valueToUpdateIfNewNotNull(final Object actualValue, final Object newValue) {

        if (newValue == null) {
            return actualValue;
        }
        return newValue;

    }

    private void setProperties(final PersonBean personBean) {
        final String fullName = personBean.getName();
        final String familyName = personBean.getFamilyNames();
        final String givenNames = personBean.getGivenNames();
        final String composedName = familyName == null || familyName.isEmpty() ? givenNames : givenNames + " " + familyName;

        // personal info
        if (givenNames != null || familyName != null) {
            ensureUserProfile();
            getProfile().changeName(givenNames, familyName, null);
        } else {
            if ((givenNames != null || familyName != null) && !fullName.equals(composedName)) {
                throw new DomainException("error.person.splittedNamesDoNotMatch");
            }
            setNames(fullName, givenNames, familyName);
        }

        setGender(personBean.getGender());
        setProfession(personBean.getProfession());
        setMaritalStatus(personBean.getMaritalStatus());

        // identification
        setIdentification(personBean.getDocumentIdNumber(), personBean.getIdDocumentType());
        setEmissionLocationOfDocumentId(personBean.getDocumentIdEmissionLocation());
        setEmissionDateOfDocumentIdYearMonthDay(personBean.getDocumentIdEmissionDate());
        setExpirationDateOfDocumentIdYearMonthDay(personBean.getDocumentIdExpirationDate());
        setSocialSecurityNumber(personBean.getSocialSecurityNumber());
        setEidentifier(personBean.getEidentifier());

        // filiation
        setDateOfBirthYearMonthDay(personBean.getDateOfBirth());
        setCountry(personBean.getNationality());
        setParishOfBirth(personBean.getParishOfBirth());
        setDistrictSubdivisionOfBirth(personBean.getDistrictSubdivisionOfBirth());
        setDistrictOfBirth(personBean.getDistrictOfBirth());
        setCountryOfBirth(personBean.getCountryOfBirth());
        setNameOfMother(personBean.getMotherName());
        setNameOfFather(personBean.getFatherName());
    }

    /***************************************************************************
     * OTHER METHODS *
     **************************************************************************/

    public String getSlideName() {
        return "/photos/person/P" + getExternalId();
    }

    public String getSlideNameForCandidateDocuments() {
        return "/candidateDocuments/person/P" + getExternalId();
    }

    public List<PersonFunction> getActivePersonFunctions() {
        return getPersonFunctions(null, false, true, false);
    }

    public List<PersonFunction> getInactivePersonFunctions() {
        return getPersonFunctions(null, false, false, false);
    }

    public List<Function> getActiveInherentPersonFunctions() {
        final List<Function> inherentFunctions = new ArrayList<Function>();
        for (final PersonFunction accountability : getActivePersonFunctions()) {
            inherentFunctions.addAll(accountability.getFunction().getInherentFunctionsSet());
        }
        return inherentFunctions;
    }

    /**
     * The main difference between this method and {@link #getActivePersonFunctions()} is that person functions with a virtual
     * function are also included. This method also collects person functions from the given unit and all subunits.
     * 
     * @see Function#isVirtual()
     */
    public List<PersonFunction> getAllActivePersonFunctions(final Unit unit) {
        return getPersonFunctions(unit, true, true, null);
    }

    public boolean containsActivePersonFunction(final Function function) {
        for (final PersonFunction personFunction : getActivePersonFunctions()) {
            if (personFunction.getFunction().equals(function)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyPersonFunctions() {
        return !getPersonFunctions().isEmpty();
    }

    public Collection<PersonFunction> getAllActivePersonFunctions(final FunctionType functionType) {
        final Set<PersonFunction> personFunctions = new HashSet<PersonFunction>();
        for (final PersonFunction personFunction : getActivePersonFunctions()) {
            if (personFunction.getFunction().isOfFunctionType(functionType)) {
                personFunctions.add(personFunction);
            }
        }
        return personFunctions;
    }

    public Collection<PersonFunction> getPersonFunctions() {
        return (Collection<PersonFunction>) getParentAccountabilities(AccountabilityTypeEnum.MANAGEMENT_FUNCTION,
                PersonFunction.class);
    }

    public Collection<PersonFunction> getPersonFunctions(final Function function) {

        final Collection<PersonFunction> personFunctions = getPersonFunctions();
        final Iterator<PersonFunction> iterator = personFunctions.iterator();

        while (iterator.hasNext()) {
            final PersonFunction element = iterator.next();
            if (element.getFunction() == function) {
                continue;
            }
            iterator.remove();
        }

        return personFunctions;
    }

    public List<PersonFunction> getPersonFuntions(final YearMonthDay begin, final YearMonthDay end) {
        return getPersonFuntions(AccountabilityTypeEnum.MANAGEMENT_FUNCTION, begin, end);
    }

    public List<PersonFunction> getPersonFunctions(final Unit unit, final boolean includeSubUnits, final Boolean active,
            final Boolean virtual) {
        return getPersonFunctions(unit, includeSubUnits, active, virtual, AccountabilityTypeEnum.MANAGEMENT_FUNCTION);
    }

    public boolean hasActivePersonFunction(final FunctionType functionType, final Unit unit) {
        final YearMonthDay currentDate = new YearMonthDay();
        for (final PersonFunction personFunction : (Collection<PersonFunction>) getParentAccountabilities(
                AccountabilityTypeEnum.MANAGEMENT_FUNCTION, PersonFunction.class)) {
            if (personFunction.getUnit().equals(unit) && personFunction.getFunction().getFunctionType() == functionType
                    && personFunction.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public Collection<PersonFunction> getPersonFunctions(final AccountabilityTypeEnum accountabilityTypeEnum) {
        return (Collection<PersonFunction>) getParentAccountabilities(accountabilityTypeEnum, PersonFunction.class);
    }

    public List<PersonFunction> getPersonFuntions(final AccountabilityTypeEnum accountabilityTypeEnum, final YearMonthDay begin,
            final YearMonthDay end) {
        final List<PersonFunction> result = new ArrayList<PersonFunction>();
        for (final Accountability accountability : (Collection<PersonFunction>) getParentAccountabilities(accountabilityTypeEnum,
                PersonFunction.class)) {
            if (accountability.belongsToPeriod(begin, end)) {
                result.add((PersonFunction) accountability);
            }
        }
        return result;
    }

    public List<PersonFunction> getPersonFunctions(final Unit unit) {
        return getPersonFunctions(unit, false, null, null);
    }

    /**
     * Filters all parent PersonFunction accountabilities and returns all the PersonFunctions that selection indicated in the
     * parameters.
     * 
     * @param unit
     *            filter all PersonFunctions to this unit, or <code>null</code> for all PersonFunctions
     * @param includeSubUnits
     *            if even subunits of the given unit are considered
     * @param active
     *            the state of the function, <code>null</code> for all PersonFunctions
     */
    public List<PersonFunction> getPersonFunctions(final Unit unit, final boolean includeSubUnits, final Boolean active,
            final Boolean virtual, final AccountabilityTypeEnum accountabilityTypeEnum) {
        final List<PersonFunction> result = new ArrayList<PersonFunction>();

        Collection<Unit> allSubUnits = Collections.emptyList();
        if (includeSubUnits) {
            allSubUnits = unit.getAllSubUnits();
        }

        final YearMonthDay today = new YearMonthDay();

        for (final PersonFunction personFunction : getPersonFunctions(accountabilityTypeEnum)) {
            if (active != null && personFunction.isActive(today) == !active) {
                continue;
            }

            if (virtual != null && personFunction.getFunction().isVirtual() == !virtual) {
                continue;
            }

            final Unit functionUnit = personFunction.getUnit();
            if (unit == null || functionUnit.equals(unit) || includeSubUnits && allSubUnits.contains(functionUnit)) {
                result.add(personFunction);
            }
        }

        return result;
    }

    public List<PersonFunction> getPersonFunctions(final Party party, final boolean includeSubUnits, final Boolean active,
            final Boolean virtual, final AccountabilityTypeEnum accountabilityTypeEnum) {
        if (party.isUnit()) {
            return getPersonFunctions((Unit) party, includeSubUnits, active, virtual, AccountabilityTypeEnum.MANAGEMENT_FUNCTION);
        }
        final List<PersonFunction> result = new ArrayList<PersonFunction>();

        final YearMonthDay today = new YearMonthDay();
        for (final PersonFunction personFunction : getPersonFunctions(accountabilityTypeEnum)) {
            if (active != null && personFunction.isActive(today) == !active) {
                continue;
            }
            if (virtual != null && personFunction.getFunction().isVirtual() == !virtual) {
                continue;
            }
            if (personFunction.getParentParty().isPerson()) {
                final Person functionPerson = (Person) personFunction.getParentParty();
                if (party == null || functionPerson.equals(party)) {
                    result.add(personFunction);
                }
            }
        }

        return result;
    }

    public boolean hasFunctionType(final FunctionType functionType, final AccountabilityTypeEnum accountabilityTypeEnum) {
        for (final PersonFunction accountability : getPersonFunctions(null, false, true, false, accountabilityTypeEnum)) {
            if (accountability.getFunction().getFunctionType() == functionType) {
                return true;
            }
        }
        return false;
    }

    public PersonFunction addPersonFunction(final Function function, final YearMonthDay begin, final YearMonthDay end,
            final Double credits) {
        return new PersonFunction(function.getUnit(), this, function, begin, end, credits);
    }

    /**
     * @return a group that only contains this person
     */
    public Group getPersonGroup() {
        return UserGroup.of(this.getUser());
    }

    /**
     * 
     * IMPORTANT: This method is evil and should NOT be used! You are NOT God!
     * 
     * 
     * @return true if the person have been deleted, false otherwise
     */
    @Override
    public void delete() {
        DomainException.throwWhenDeleteBlocked(getDeletionBlockers());
        if (getPersonalPhotoEvenIfRejected() != null) {
            getPersonalPhotoEvenIfRejected().delete();
        }
        if (getAssociatedPersonAccount() != null) {
            getAssociatedPersonAccount().delete();
        }

        /*
         * One does not simply delete a User...
         */
//        if (hasUser()) {
//            getUser().delete();
//        }

        if (getStudent() != null) {
            getStudent().delete();
        }
        if (getPersonName() != null) {
            getPersonName().delete();
        }

        getManageableDepartmentCreditsSet().clear();
        getThesisEvaluationParticipantsSet().clear();

        for (; !getIdDocumentsSet().isEmpty(); getIdDocumentsSet().iterator().next().delete()) {
            ;
        }
        for (; !getScientificCommissionsSet().isEmpty(); getScientificCommissionsSet().iterator().next().delete()) {
            ;
        }

        setNationality(null);
        setCountryOfBirth(null);

        if (getResearcher() != null) {
            getResearcher().delete();
        }
        super.delete();
    }

    @Override
    protected void checkForDeletionBlockers(Collection<String> blockers) {
        super.checkForDeletionBlockers(blockers);
        if (!(getPartyContactsSet().isEmpty() && getChildsSet().isEmpty() && getParentsSet().isEmpty()
                && getExportGroupingReceiversSet().isEmpty() && getPersistentGroupsSet().isEmpty()
                && getAssociatedQualificationsSet().isEmpty() && getAssociatedAlteredCurriculumsSet().isEmpty()
                && getEnrolmentEvaluationsSet().isEmpty() && getExportGroupingSendersSet().isEmpty()
                && getResponsabilityTransactionsSet().isEmpty() && getGuidesSet().isEmpty() && getEmployee() == null
                && getTeacher() == null && !hasAnyPersonFunctions() && getInternalParticipantsSet().isEmpty()
                && getCreatedQualificationsSet().isEmpty() && getCreateJobsSet().isEmpty())) {
            blockers.add(BundleUtil.getString(Bundle.APPLICATION, "error.person.cannot.be.deleted"));
        }
    }

    public ExternalContract getExternalContract() {
        final Collection<ExternalContract> externalContracts =
                (Collection<ExternalContract>) getParentAccountabilities(AccountabilityTypeEnum.WORKING_CONTRACT,
                        ExternalContract.class);

        final Iterator<ExternalContract> iter = externalContracts.iterator();
        return iter.hasNext() ? externalContracts.iterator().next() : null;
    }

    public boolean hasExternalContract() {
        return getExternalContract() != null;
    }

    public ResearchContract getExternalResearchContract() {
        final Collection<ResearchContract> externalContracts =
                (Collection<ResearchContract>) getParentAccountabilities(AccountabilityTypeEnum.RESEARCH_CONTRACT,
                        ResearchContract.class);

        final Iterator<ResearchContract> iter = externalContracts.iterator();
        if (iter.hasNext()) {
            final ResearchContract contract = externalContracts.iterator().next();
            if (Boolean.TRUE.equals(contract.getExternalContract())) {
                return contract;
            }
        }
        return null;
    }

    public boolean hasExternalResearchContract() {
        return getExternalResearchContract() != null;
    }

    @Override
    public void setDisableSendEmails(Boolean disableSendEmails) {
        ensureUserProfile();
        super.setDisableSendEmails(disableSendEmails);
        getProfile().setEmail(getEmailForSendingEmails());
    }

    @Deprecated
    public Registration readStudentByDegreeType(final DegreeType degreeType) {
        for (final Registration registration : this.getStudents()) {
            if (registration.getDegreeType().equals(degreeType)) {
                return registration;
            }
        }
        return null;
    }

    public Registration readRegistrationByDegreeCurricularPlan(final DegreeCurricularPlan degreeCurricularPlan) {
        return getStudent().readRegistrationByDegreeCurricularPlan(degreeCurricularPlan);
    }

    public DFACandidacy getDFACandidacyByExecutionDegree(final ExecutionDegree executionDegree) {
        for (final Candidacy candidacy : this.getCandidaciesSet()) {
            if (candidacy instanceof DFACandidacy) {
                final DFACandidacy dfaCandidacy = (DFACandidacy) candidacy;
                if (dfaCandidacy.getExecutionDegree().equals(executionDegree)) {
                    return dfaCandidacy;
                }
            }
        }
        return null;
    }

    public DegreeCandidacy getDegreeCandidacyByExecutionDegree(final ExecutionDegree executionDegree) {
        for (final Candidacy candidacy : this.getCandidaciesSet()) {
            if (candidacy instanceof DegreeCandidacy && candidacy.isActive()) {
                final DegreeCandidacy degreeCandidacy = (DegreeCandidacy) candidacy;
                if (degreeCandidacy.getExecutionDegree().equals(executionDegree)) {
                    return degreeCandidacy;
                }
            }
        }
        return null;
    }

    public List<DegreeCandidacy> getDegreeCandidaciesFor(final ExecutionYear executionYear,
            final CandidacySituationType candidacySituationType) {

        final List<DegreeCandidacy> result = new ArrayList<DegreeCandidacy>();
        for (final Candidacy candidacy : this.getCandidaciesSet()) {
            if (candidacy instanceof DegreeCandidacy) {
                final DegreeCandidacy degreeCandidacy = (DegreeCandidacy) candidacy;
                if (degreeCandidacy.getActiveCandidacySituation().getCandidacySituationType() == candidacySituationType
                        && degreeCandidacy.getExecutionDegree().getExecutionYear() == executionYear) {

                    result.add((DegreeCandidacy) candidacy);
                }
            }
        }

        return result;
    }

    public boolean hasDegreeCandidacyForExecutionDegree(final ExecutionDegree executionDegree) {
        return getDegreeCandidacyByExecutionDegree(executionDegree) != null;
    }

    public StudentCandidacy getStudentCandidacyForExecutionDegree(final ExecutionDegree executionDegree) {
        for (final Candidacy candidacy : this.getCandidaciesSet()) {
            if (candidacy instanceof StudentCandidacy && candidacy.isActive()) {
                if (candidacy instanceof PHDProgramCandidacy) {
                    continue;
                }

                final StudentCandidacy studentCandidacy = (StudentCandidacy) candidacy;
                if (studentCandidacy.getExecutionDegree().equals(executionDegree)) {
                    return studentCandidacy;
                }
            }
        }
        return null;
    }

    public boolean hasStudentCandidacyForExecutionDegree(final ExecutionDegree executionDegree) {
        return getStudentCandidacyForExecutionDegree(executionDegree) != null;
    }

    public StudentCandidacy getSomeStudentCandidacyForExecutionDegree(final ExecutionDegree executionDegree) {
        for (final Candidacy candidacy : this.getCandidaciesSet()) {
            if (candidacy instanceof StudentCandidacy) {
                if (candidacy instanceof PHDProgramCandidacy) {
                    continue;
                }

                final StudentCandidacy studentCandidacy = (StudentCandidacy) candidacy;
                if (studentCandidacy.getExecutionDegree().equals(executionDegree)) {
                    return studentCandidacy;
                }
            }
        }
        return null;
    }

    public boolean hasSomeStudentCandidacyForExecutionDegree(final ExecutionDegree executionDegree) {
        return getSomeStudentCandidacyForExecutionDegree(executionDegree) != null;
    }

    public Collection<Invitation> getInvitationsOrderByDate() {
        final Set<Invitation> invitations = new TreeSet<Invitation>(Invitation.CONTRACT_COMPARATOR_BY_BEGIN_DATE);
        invitations
                .addAll((Collection<Invitation>) getParentAccountabilities(AccountabilityTypeEnum.INVITATION, Invitation.class));
        return invitations;
    }

    public List<Invitation> getActiveInvitations() {
        final YearMonthDay today = new YearMonthDay();
        final List<Invitation> invitations = new ArrayList<Invitation>();
        for (final Accountability accoutAccountability : getParentAccountabilities(AccountabilityTypeEnum.INVITATION,
                Invitation.class)) {
            if (((Invitation) accoutAccountability).isActive(today)) {
                invitations.add((Invitation) accoutAccountability);
            }
        }
        return invitations;
    }

    public boolean isInvited(final YearMonthDay date) {
        for (final Invitation invitation : (Collection<Invitation>) getParentAccountabilities(AccountabilityTypeEnum.INVITATION,
                Invitation.class)) {
            if (invitation.isActive(date)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyInvitation() {
        return !getParentAccountabilities(AccountabilityTypeEnum.INVITATION, Invitation.class).isEmpty();
    }

    // -------------------------------------------------------------
    // static methods
    // -------------------------------------------------------------

    public static Person readPersonByUsername(final String username) {
        final User user = User.findByUsername(username);
        return user == null ? null : user.getPerson();
    }

    public static Collection<Person> readByDocumentIdNumber(final String documentIdNumber) {
        final Collection<Person> result = new HashSet<Person>();
        for (final IdDocument idDocument : IdDocument.find(documentIdNumber)) {
            result.add(idDocument.getPerson());
        }
        return result;
    }

    public static Person readByDocumentIdNumberAndIdDocumentType(final String documentIdNumber,
            final IDDocumentType idDocumentType) {
        for (final IdDocument idDocument : IdDocument.find(documentIdNumber)) {
            if (idDocument.getIdDocumentType().getValue() == idDocumentType) {
                return idDocument.getPerson();
            }
        }
        return null;
    }

    public static Person readByDocumentIdNumberAndDateOfBirth(final String documentIdNumber, final YearMonthDay dateOfBirth) {
        for (final IdDocument idDocument : IdDocument.find(documentIdNumber)) {
            final Person person = idDocument.getPerson();
            if (person.getDateOfBirthYearMonthDay().equals(dateOfBirth)) {
                return person;
            }
        }
        return null;
    }

    public static Collection<Person> findByDateOfBirth(final YearMonthDay dateOfBirth, final Collection<Person> persons) {
        final List<Person> result = new ArrayList<Person>();
        for (final Person person : persons) {
            if (person.getDateOfBirthYearMonthDay() == null || person.getDateOfBirthYearMonthDay().equals(dateOfBirth)) {
                result.add(person);
            }
        }
        return result;
    }

    // used by grant owner
    public static List<Person> readPersonsByName(final String name, final Integer startIndex, final Integer numberOfElementsInSpan) {
        final Collection<Person> personsList = readPersonsByName(name, Integer.MAX_VALUE);
        if (startIndex != null && numberOfElementsInSpan != null && !personsList.isEmpty()) {
            final int finalIndex = Math.min(personsList.size(), startIndex + numberOfElementsInSpan);
            final List<Person> result = new ArrayList<Person>(finalIndex - startIndex);
            final Iterator<Person> iter = personsList.iterator();
            for (int i = 0; i <= finalIndex && iter.hasNext(); i++) {
                final Person person = iter.next();
                if (i >= startIndex) {
                    result.add(person);
                }
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    public static Integer countAllByName(final String name) {
        return readPersonsByName(name, Integer.MAX_VALUE).size();
    }

    public static Collection<Person> readPersonsByName(final String name, final int size) {
        return findPerson(name.replace('%', ' '), size);
    }

    public static Stream<Person> findPersonStream(final String name, final int size) {
        return Stream.concat(PersonName.findPersonStream(name, size).map(n -> n.getPerson()), UserProfile
                .searchByName(name, size).map(p -> p.getPerson()).filter(Objects::nonNull));
    }

    public static Stream<Person> findInternalPersonStream(final String name, final int size) {
        return Stream.concat(
                PersonName.findInternalPersonStream(name, size).map(n -> n.getPerson()),
                UserProfile.searchByName(name, size).map(p -> p.getPerson()).filter(Objects::nonNull)
                        .filter(p -> !p.isExternalPerson()));
    }

    public static Stream<Person> findExternalPersonStream(final String name, final int size) {
        return Stream.concat(
                PersonName.findExternalPersonStream(name, size).map(n -> n.getPerson()),
                UserProfile.searchByName(name, size).map(p -> p.getPerson()).filter(Objects::nonNull)
                        .filter(p -> p.isExternalPerson()));
    }

    public static Collection<Person> findPerson(final String name, final int size) {
        return findPersonStream(name, size).collect(Collectors.toSet());
    }

    public static Collection<Person> findPerson(final String name, final int size,
            final com.google.common.base.Predicate<Person> predicate) {
        return findPersonStream(name, size).filter(p -> predicate.apply(p)).collect(Collectors.toSet());
    }

    public static Collection<Person> readPersonsByName(final String name) {
        return findPerson(name.replace('%', ' '));
    }

    public static List<Person> readAllPersons() {
        final List<Person> allPersons = new ArrayList<Person>();
        for (final Party party : Bennu.getInstance().getPartysSet()) {
            if (party.isPerson()) {
                allPersons.add((Person) party);
            }
        }
        return allPersons;
    }

    public static Collection<Person> readPersonsByNameAndRoleType(final String name, final RoleType roleType) {
        final Collection<Person> people = findPerson(name);
        for (final Iterator<Person> iter = people.iterator(); iter.hasNext();) {
            final Person person = iter.next();
            if (!person.hasRole(roleType)) {
                iter.remove();
            }
        }
        return people;
    }

    public SortedSet<StudentCurricularPlan> getActiveStudentCurricularPlansSortedByDegreeTypeAndDegreeName() {
        final SortedSet<StudentCurricularPlan> studentCurricularPlans =
                new TreeSet<StudentCurricularPlan>(
                        StudentCurricularPlan.STUDENT_CURRICULAR_PLAN_COMPARATOR_BY_DEGREE_TYPE_AND_DEGREE_NAME);
        for (final Registration registration : getStudentsSet()) {
            final StudentCurricularPlan studentCurricularPlan = registration.getActiveStudentCurricularPlan();
            if (studentCurricularPlan != null) {
                studentCurricularPlans.add(studentCurricularPlan);
            }
        }
        return studentCurricularPlans;
    }

    public SortedSet<StudentCurricularPlan> getCompletedStudentCurricularPlansSortedByDegreeTypeAndDegreeName() {
        final SortedSet<StudentCurricularPlan> studentCurricularPlans =
                new TreeSet<StudentCurricularPlan>(
                        StudentCurricularPlan.STUDENT_CURRICULAR_PLAN_COMPARATOR_BY_DEGREE_TYPE_AND_DEGREE_NAME);

        for (final Registration registration : getStudentsSet()) {
            if (registration.isConcluded()) {
                final StudentCurricularPlan lastStudent = registration.getLastStudentCurricularPlan();
                if (lastStudent != null) {
                    studentCurricularPlans.add(lastStudent);
                }
            }
        }
        return studentCurricularPlans;
    }

    public Set<Attends> getCurrentAttends() {
        final Set<Attends> attends = new HashSet<Attends>();
        for (final Registration registration : getStudentsSet()) {
            for (final Attends attend : registration.getAssociatedAttendsSet()) {
                final ExecutionCourse executionCourse = attend.getExecutionCourse();
                final ExecutionSemester executionSemester = executionCourse.getExecutionPeriod();
                if (executionSemester.getState().equals(PeriodState.CURRENT)) {
                    attends.add(attend);
                }
            }
        }
        return attends;
    }

    public Set<Attends> getCurrentAttendsPlusSpecialSeason() {
        final Set<Attends> attends = new HashSet<Attends>();
        for (final Registration registration : getStudentsSet()) {
            for (final Attends attend : registration.getAssociatedAttendsSet()) {
                final ExecutionCourse executionCourse = attend.getExecutionCourse();
                final ExecutionSemester executionSemester = executionCourse.getExecutionPeriod();
                if (executionSemester.getState().equals(PeriodState.CURRENT)) {
                    attends.add(attend);
                } else if (attend.getEnrolment() != null && attend.getEnrolment().isSpecialSeason()) {
                    if (executionSemester.getNextExecutionPeriod().getState().equals(PeriodState.CURRENT)) {
                        attends.add(attend);
                    }
                }
            }
        }
        return attends;
    }

    private Set<Event> getEventsFromType(final Class<? extends Event> clazz) {
        final Set<Event> events = new HashSet<Event>();

        for (final Event event : getEventsSet()) {
            if (clazz.isAssignableFrom(event.getClass())) {
                events.add(event);
            }
        }

        return events;
    }

    public Set<Event> getAcademicEvents() {
        return getEventsFromType(AcademicEvent.class);
    }

    public Set<Event> getResidencePaymentEvents() {
        return getEventsFromType(ResidenceEvent.class);
    }

    public Set<Event> getNotPayedEventsPayableOn(final AdministrativeOffice administrativeOffice, final Class eventClass,
            final boolean withInstallments) {
        final Set<Event> result = new HashSet<Event>();

        Set<Event> events = getEventsFromType(eventClass);
        for (final Event event : events) {
            if (event.isOpen() && event.hasInstallments() == withInstallments
                    && isPayableOnAnyOfAdministrativeOffices(Collections.singleton(administrativeOffice), event)) {
                result.add(event);
            }
        }

        return result;
    }

    public Set<Event> getNotPayedEventsPayableOn(final AdministrativeOffice administrativeOffice, final boolean withInstallments) {
        return getNotPayedEventsPayableOn(administrativeOffice, AcademicEvent.class, withInstallments);
    }

    public Set<Event> getNotPayedEventsPayableOn(final AdministrativeOffice administrativeOffice) {
        final Set<Event> result = new HashSet<Event>();
        for (final Event event : getAcademicEvents()) {
            if (event.isOpen() && isPayableOnAnyOfAdministrativeOffices(Collections.singleton(administrativeOffice), event)) {
                result.add(event);
            }
        }

        return result;
    }

    public Set<Event> getNotPayedEvents() {
        final Set<Event> result = new HashSet<Event>();
        for (final Event event : getAcademicEvents()) {
            if (event.isOpen()) {
                result.add(event);
            }
        }

        return result;
    }

    private boolean isPayableOnAnyOfAdministrativeOffices(final Set<AdministrativeOffice> administrativeOffices, final Event event) {

        if (administrativeOffices == null) {
            return true;
        }

        for (final AdministrativeOffice administrativeOffice : administrativeOffices) {
            if (administrativeOffice == null || event.isPayableOnAdministrativeOffice(administrativeOffice)) {
                return true;
            }
        }

        return false;
    }

    public List<Event> getPayedEvents(final Class eventClass) {
        final List<Event> result = new ArrayList<Event>();
        Set<Event> events = getEventsFromType(eventClass);
        for (final Event event : events) {
            if (event.isClosed()) {
                result.add(event);
            }
        }

        return result;
    }

    public List<Event> getPayedEvents() {
        return getPayedEvents(AcademicEvent.class);
    }

    public List<Event> getEventsWithPayments() {
        final List<Event> result = new ArrayList<Event>();
        for (final Event event : getAcademicEvents()) {
            if (!event.isCancelled() && event.hasAnyPayments()) {
                result.add(event);
            }
        }

        return result;
    }

    public Set<AccountingTransaction> getPaymentTransactions(final EventType... type) {
        final Set<AccountingTransaction> transactions = new HashSet<AccountingTransaction>();
        final List<EventType> types = Arrays.asList(type);
        for (final Event event : getEventsSet()) {
            if (!event.isCancelled() && types.contains(event.getEventType())) {
                transactions.addAll(event.getNonAdjustingTransactions());
            }
        }
        return transactions;
    }

    public Set<Entry> getPaymentsWithoutReceipt() {
        return getPaymentsWithoutReceiptByAdministrativeOffices(null);
    }

    public Set<Entry> getPaymentsWithoutReceiptByAdministrativeOffices(final Set<AdministrativeOffice> administrativeOffices) {
        final Set<Entry> result = new HashSet<Entry>();

        for (final Event event : getAcademicEvents()) {
            if (!event.isCancelled() && isPayableOnAnyOfAdministrativeOffices(administrativeOffices, event)) {
                result.addAll(event.getEntriesWithoutReceipt());
            }
        }

        return result;
    }

    public Set<Entry> getPayments(final Class eventClass) {
        final Set<Entry> result = new HashSet<Entry>();
        Set<Event> events = getEventsFromType(eventClass);
        for (final Event event : events) {
            if (!event.isCancelled()) {
                result.addAll(event.getPositiveEntries());
            }
        }
        return result;
    }

    public Set<Entry> getPayments() {
        return getPayments(AcademicEvent.class);
    }

    public Money getTotalPaymentsAmountWithAdjustment() {
        Money total = new Money(0);
        for (final Entry entry : getPayments(AcademicEvent.class)) {
            total = total.add(entry.getAmountWithAdjustment());
        }
        return total;
    }

    public Set<? extends Event> getEventsByEventTypes(final EventType... eventTypes) {
        return getEventsByEventTypes(Arrays.asList(eventTypes));
    }

    public Set<? extends Event> getEventsByEventTypes(final Collection<EventType> eventTypes) {

        final Set<Event> result = new HashSet<Event>();

        for (final EventType eventType : eventTypes) {
            for (final Event event : getAcademicEvents()) {
                if (!event.isCancelled() && event.getEventType() == eventType) {
                    result.add(event);
                }
            }
        }

        return result;

    }

    public Set<? extends Event> getEventsByEventType(final EventType eventType) {
        return getEventsByEventTypeAndClass(eventType, null);
    }

    public Set<? extends Event> getEventsByEventTypeAndClass(final EventType eventType, final Class<? extends Event> clazz) {
        final Set<Event> result = new HashSet<Event>();

        for (final Event event : getEventsSet()) {
            if (!event.isCancelled() && event.getEventType() == eventType && (clazz == null || event.getClass().equals(clazz))) {
                result.add(event);
            }
        }

        return result;
    }

    public Set<AnnualEvent> getAnnualEventsFor(final ExecutionYear executionYear) {
        final Set<AnnualEvent> result = new HashSet<AnnualEvent>();
        for (final Event event : getEventsSet()) {
            if (event instanceof AnnualEvent) {
                final AnnualEvent annualEvent = (AnnualEvent) event;
                if (annualEvent.isFor(executionYear) && !annualEvent.isCancelled()) {
                    result.add(annualEvent);
                }
            }
        }

        return result;
    }

    public Set<AnnualEvent> getOpenAnnualEventsFor(final ExecutionYear executionYear) {
        final Set<AnnualEvent> result = new HashSet<AnnualEvent>();
        for (final Event event : getEventsSet()) {
            if (event instanceof AnnualEvent) {
                final AnnualEvent annualEvent = (AnnualEvent) event;
                if (annualEvent.isFor(executionYear) && annualEvent.isOpen()) {
                    result.add(annualEvent);
                }
            }
        }

        return result;
    }

    public boolean hasInsuranceEventOrAdministrativeOfficeFeeInsuranceEventFor(final ExecutionYear executionYear) {
        return hasInsuranceEventFor(executionYear) || hasAdministrativeOfficeFeeInsuranceEventFor(executionYear);
    }

    public Set<InsuranceEvent> getNotCancelledInsuranceEvents() {
        final Set<InsuranceEvent> result = new HashSet<InsuranceEvent>();

        for (final Event event : getEventsByEventType(EventType.INSURANCE)) {
            final InsuranceEvent specificEvent = (InsuranceEvent) event;
            if (!specificEvent.isCancelled()) {
                result.add(specificEvent);
            }
        }

        return result;
    }

    public Set<InsuranceEvent> getNotCancelledInsuranceEventsUntil(final ExecutionYear executionYear) {
        final Set<InsuranceEvent> result = new HashSet<InsuranceEvent>();

        for (final Event event : getEventsByEventType(EventType.INSURANCE)) {
            final InsuranceEvent specificEvent = (InsuranceEvent) event;
            if (!specificEvent.isCancelled() && specificEvent.getExecutionYear().isBeforeOrEquals(executionYear)) {
                result.add(specificEvent);
            }
        }

        return result;
    }

    public InsuranceEvent getInsuranceEventFor(final ExecutionYear executionYear) {
        for (final Event event : getEventsByEventType(EventType.INSURANCE)) {
            final InsuranceEvent insuranceEvent = (InsuranceEvent) event;
            if (!insuranceEvent.isCancelled() && insuranceEvent.isFor(executionYear)) {
                return insuranceEvent;
            }
        }

        return null;

    }

    public boolean hasInsuranceEventFor(final ExecutionYear executionYear) {
        return getInsuranceEventFor(executionYear) != null;
    }

    public Set<AdministrativeOfficeFeeAndInsuranceEvent> getNotCancelledAdministrativeOfficeFeeAndInsuranceEvents(
            final AdministrativeOffice office) {
        final Set<AdministrativeOfficeFeeAndInsuranceEvent> result = new HashSet<AdministrativeOfficeFeeAndInsuranceEvent>();

        for (final Event event : getEventsByEventType(EventType.ADMINISTRATIVE_OFFICE_FEE_INSURANCE)) {
            final AdministrativeOfficeFeeAndInsuranceEvent specificEvent = (AdministrativeOfficeFeeAndInsuranceEvent) event;
            if (!specificEvent.isCancelled() && specificEvent.getAdministrativeOffice() == office) {
                result.add(specificEvent);
            }
        }

        return result;
    }

    public Set<AdministrativeOfficeFeeAndInsuranceEvent> getNotCancelledAdministrativeOfficeFeeAndInsuranceEventsUntil(
            final AdministrativeOffice office, final ExecutionYear executionYear) {
        final Set<AdministrativeOfficeFeeAndInsuranceEvent> result = new HashSet<AdministrativeOfficeFeeAndInsuranceEvent>();

        for (final Event event : getEventsByEventType(EventType.ADMINISTRATIVE_OFFICE_FEE_INSURANCE)) {
            final AdministrativeOfficeFeeAndInsuranceEvent specificEvent = (AdministrativeOfficeFeeAndInsuranceEvent) event;
            if (!specificEvent.isCancelled() && specificEvent.getAdministrativeOffice() == office
                    && specificEvent.getExecutionYear().isBeforeOrEquals(executionYear)) {
                result.add(specificEvent);
            }
        }

        return result;
    }

    public AdministrativeOfficeFeeAndInsuranceEvent getAdministrativeOfficeFeeInsuranceEventFor(final ExecutionYear executionYear) {
        for (final Event event : getEventsByEventType(EventType.ADMINISTRATIVE_OFFICE_FEE_INSURANCE)) {
            final AdministrativeOfficeFeeAndInsuranceEvent administrativeOfficeFeeAndInsuranceEvent =
                    (AdministrativeOfficeFeeAndInsuranceEvent) event;
            if (!administrativeOfficeFeeAndInsuranceEvent.isCancelled()
                    && administrativeOfficeFeeAndInsuranceEvent.isFor(executionYear)) {
                return administrativeOfficeFeeAndInsuranceEvent;
            }
        }

        return null;
    }

    public boolean hasAdministrativeOfficeFeeInsuranceEventFor(final ExecutionYear executionYear) {
        return getAdministrativeOfficeFeeInsuranceEventFor(executionYear) != null;
    }

    public Set<Event> getEventsSupportingPaymentByOtherParties() {
        final Set<Event> result = new HashSet<Event>();
        for (final Event event : getEventsSet()) {
            if (!event.isCancelled() && event.isOtherPartiesPaymentsSupported()) {
                result.add(event);
            }
        }

        return result;
    }

    public List<PaymentCode> getPaymentCodesBy(final PaymentCodeType paymentCodeType) {
        final List<PaymentCode> result = new ArrayList<PaymentCode>();
        for (final PaymentCode paymentCode : getPaymentCodesSet()) {
            if (paymentCode.getType() == paymentCodeType) {
                result.add(paymentCode);
            }
        }

        return result;
    }

    public PaymentCode getPaymentCodeBy(final String code) {
        for (final PaymentCode paymentCode : getPaymentCodesSet()) {
            if (paymentCode.getCode().equals(code)) {
                return paymentCode;
            }
        }

        return null;
    }

    public Set<GratuityEvent> getGratuityEvents() {
        return (Set<GratuityEvent>) getEventsByEventTypes(EventType.getGratuityEventTypes());
    }

    public List<Event> getEventsWithExemptionAppliable() {
        final List<Event> result = new ArrayList<Event>();
        for (final Event event : getEventsSet()) {
            if (!event.isCancelled() && event.isExemptionAppliable()) {
                result.add(event);
            }
        }

        return result;
    }

    public Money getMaxDeductableAmountForLegalTaxes(final EventType eventType, final int civilYear) {
        Money result = Money.ZERO;
        for (final Event event : (Set<Event>) getEventsByEventType(eventType)) {
            result = result.add(event.getMaxDeductableAmountForLegalTaxes(civilYear));
        }

        return result;
    }

    public Set<Receipt> getReceiptsByAdministrativeOffices(final Set<AdministrativeOffice> administrativeOffices) {
        final Set<Receipt> result = new HashSet<Receipt>();
        for (final Receipt receipt : getReceiptsSet()) {
            for (final AdministrativeOffice administrativeOffice : administrativeOffices) {
                if (receipt.isFromAdministrativeOffice(administrativeOffice)) {
                    result.add(receipt);
                }
            }
        }

        return result;
    }

    @Override
    final public boolean isPerson() {
        return true;
    }

    final public boolean isFemale() {
        return getGender() == Gender.FEMALE;
    }

    final public boolean isMale() {
        return getGender() == Gender.MALE;
    }

    @Deprecated
    public Set<Registration> getStudents() {
        return getStudent() != null ? getStudent().getRegistrationsSet() : Collections.<Registration> emptySet();
    }

    @Deprecated
    public boolean hasAnyStudents() {
        return getStudentsCount() > 0;
    }

    @Deprecated
    public int getStudentsCount() {
        return getStudent() != null ? getStudent().getRegistrationsSet().size() : 0;
    }

    @Deprecated
    public Set<Registration> getStudentsSet() {
        return getStudent() != null ? getStudent().getRegistrationsSet() : Collections.EMPTY_SET;
    }

    public Set<Career> getCareersByType(final CareerType type) {
        return getCareersByTypeAndInterval(type, null);
    }

    public Set<Career> getCareersByTypeAndInterval(final CareerType type, final Interval intersecting) {
        final Set<Career> careers = new HashSet<Career>();
        for (final Career career : getAssociatedCareersSet()) {
            if (type == null || type.equals(CareerType.PROFESSIONAL) && career instanceof ProfessionalCareer
                    || type.equals(CareerType.TEACHING) && career instanceof TeachingCareer) {
                if (intersecting == null || career.getInterval().overlaps(intersecting)) {
                    careers.add(career);
                }
            }
        }
        return careers;
    }

    public static class AnyPersonSearchBean implements Serializable {
        String name;

        String documentIdNumber;

        IDDocumentType idDocumentType;

        public String getDocumentIdNumber() {
            return documentIdNumber;
        }

        public void setDocumentIdNumber(final String documentIdNumber) {
            this.documentIdNumber = documentIdNumber;
        }

        public IDDocumentType getIdDocumentType() {
            return idDocumentType;
        }

        public void setIdDocumentType(final IDDocumentType idDocumentType) {
            this.idDocumentType = idDocumentType;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        private boolean matchesAnyCriteriaField(final String[] nameValues, final String string, final String stringFromPerson) {
            return isSpecified(string) && areNamesPresent(stringFromPerson, nameValues);
        }

        public SortedSet<Person> search() {
            final SortedSet<Person> people = new TreeSet<Person>(Party.COMPARATOR_BY_NAME_AND_ID);
            if (isSpecified(name)) {
                people.addAll(findPerson(name));
            }
            if (isSpecified(documentIdNumber)) {
                for (final IdDocument idDocument : Bennu.getInstance().getIdDocumentsSet()) {
                    final String[] documentIdNumberValues =
                            documentIdNumber == null ? null : StringNormalizer.normalize(documentIdNumber).split("\\p{Space}+");
                    if (matchesAnyCriteriaField(documentIdNumberValues, documentIdNumber, idDocument.getValue())) {
                        people.add(idDocument.getPerson());
                    }
                }
            }
            return people;
        }

        public SortedSet<Person> getSearch() {
            return search();
        }

        public boolean getHasBeenSubmitted() {
            return isSpecified(name) || isSpecified(documentIdNumber);
        }

        private boolean isSpecified(final String string) {
            return string != null && string.length() > 0;
        }

        private boolean areNamesPresent(final String name, final String[] searchNameParts) {
            final String nameNormalized = StringNormalizer.normalize(name);
            for (String searchNamePart : searchNameParts) {
                final String namePart = searchNamePart;
                if (!nameNormalized.contains(namePart)) {
                    return false;
                }
            }
            return true;
        }
    }

    public Registration getRegistration(final ExecutionCourse executionCourse) {
        return executionCourse.getRegistration(this);
    }

    public SortedSet<String> getOrganizationalUnitsPresentation() {
        final SortedSet<String> organizationalUnits = new TreeSet<String>();
        for (final Accountability accountability : getParentsSet()) {
            if (isOrganizationalUnitsForPresentation(accountability)) {
                final Party party = accountability.getParentParty();
                organizationalUnits.add(party.getName());
            }
        }
        if (getStudent() != null) {
            for (final Registration registration : getStudent().getRegistrationsSet()) {
                if (registration.isActive()) {
                    final DegreeCurricularPlan degreeCurricularPlan = registration.getLastDegreeCurricularPlan();
                    if (degreeCurricularPlan != null) {
                        final Degree degree = degreeCurricularPlan.getDegree();
                        organizationalUnits.add(degree.getPresentationName());
                    }
                }
            }
        }
        return organizationalUnits;
    }

    private boolean isOrganizationalUnitsForPresentation(final Accountability accountability) {
        final AccountabilityType accountabilityType = accountability.getAccountabilityType();
        final AccountabilityTypeEnum accountabilityTypeEnum = accountabilityType.getType();
        return accountabilityTypeEnum == AccountabilityTypeEnum.WORKING_CONTRACT;
    }

    @Override
    public String getNickname() {
        if (getProfile() != null) {
            return getProfile().getDisplayName();
        }
        final String nickname = super.getNickname();
        return nickname == null ? getName() : nickname;
    }

    @Override
    @Atomic
    public void setNickname(final String nickname) {
        ensureUserProfile();
        getProfile().changeName(getProfile().getGivenNames(), getProfile().getFamilyNames(), nickname);
    }

    private static final Set<String> namePartsToIgnore = new HashSet<String>(5);
    static {
        namePartsToIgnore.add("de");
        namePartsToIgnore.add("da");
        namePartsToIgnore.add("do");
        namePartsToIgnore.add("a");
        namePartsToIgnore.add("e");
        namePartsToIgnore.add("i");
        namePartsToIgnore.add("o");
        namePartsToIgnore.add("u");
    }

    private boolean validNickname(final String name) {
        if (name != null && name.length() > 0) {
            final String normalizedName = StringNormalizer.normalize(name.replace('-', ' '));
            final String normalizedPersonName = StringNormalizer.normalize(getName().replace('-', ' '));

            final String[] nameParts = normalizedName.split(" ");
            final String[] personNameParts = normalizedPersonName.split(" ");
            int matches = 0;
            for (final String namePart : nameParts) {
                if (!contains(personNameParts, namePart)) {
                    return false;
                }
                if (!namePartsToIgnore.contains(namePart)) {
                    matches++;
                }
            }
            if (matches >= 2) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(final String[] strings, final String xpto) {
        if (xpto == null) {
            return false;
        }
        for (final String string : strings) {
            if (string.length() == xpto.length() && string.hashCode() == xpto.hashCode() && string.equals(xpto)) {
                return true;
            }
        }
        return false;
    }

    public String getHomepageWebAddress() {
        if (isDefaultWebAddressVisible() && getDefaultWebAddress().hasUrl()) {
            return getDefaultWebAddress().getUrl();
        }
        return null;
    }

    @Deprecated
    public boolean hasAvailableWebSite() {
        return getAvailableWebSite() != null && getAvailableWebSite().booleanValue();
    }

    public Collection<ExecutionDegree> getCoordinatedExecutionDegrees(final DegreeCurricularPlan degreeCurricularPlan) {
        final Set<ExecutionDegree> result = new TreeSet<ExecutionDegree>(ExecutionDegree.EXECUTION_DEGREE_COMPARATORY_BY_YEAR);
        for (final Coordinator coordinator : getCoordinatorsSet()) {
            if (coordinator.getExecutionDegree().getDegreeCurricularPlan().equals(degreeCurricularPlan)) {
                result.add(coordinator.getExecutionDegree());
            }
        }
        return result;
    }

    public boolean isCoordinatorFor(final DegreeCurricularPlan degreeCurricularPlan, final ExecutionYear executionYear) {
        for (final ExecutionDegree executionDegree : degreeCurricularPlan.getExecutionDegreesSet()) {
            if (executionDegree.getExecutionYear() == executionYear) {
                return executionDegree.getCoordinatorByTeacher(this) != null;
            }
        }
        return false;
    }

    public boolean isResponsibleOrCoordinatorFor(final CurricularCourse curricularCourse,
            final ExecutionSemester executionSemester) {
        final Teacher teacher = getTeacher();
        return teacher != null && teacher.isResponsibleFor(curricularCourse, executionSemester)
                || isCoordinatorFor(curricularCourse.getDegreeCurricularPlan(), executionSemester.getExecutionYear());
    }

    private final static List<DegreeType> degreeTypesForIsMasterDegreeOrBolonhaMasterDegreeCoordinator = Arrays
            .asList(new DegreeType[] { DegreeType.MASTER_DEGREE, DegreeType.BOLONHA_MASTER_DEGREE });

    public boolean isMasterDegreeOrBolonhaMasterDegreeCoordinatorFor(final ExecutionYear executionYear) {
        return isCoordinatorFor(executionYear, degreeTypesForIsMasterDegreeOrBolonhaMasterDegreeCoordinator);

    }

    private final static List<DegreeType> degreeTypesForisDegreeOrBolonhaDegreeOrBolonhaIntegratedMasterDegreeCoordinatorFor =
            Arrays.asList(new DegreeType[] { DegreeType.DEGREE, DegreeType.BOLONHA_DEGREE,
                    DegreeType.BOLONHA_INTEGRATED_MASTER_DEGREE });

    public boolean isDegreeOrBolonhaDegreeOrBolonhaIntegratedMasterDegreeCoordinatorFor(final ExecutionYear executionYear) {
        return isCoordinatorFor(executionYear, degreeTypesForisDegreeOrBolonhaDegreeOrBolonhaIntegratedMasterDegreeCoordinatorFor);

    }

    public boolean isCoordinatorFor(final ExecutionYear executionYear, final List<DegreeType> degreeTypes) {
        for (final Coordinator coordinator : getCoordinatorsSet()) {
            final ExecutionDegree executionDegree = coordinator.getExecutionDegree();
            if (executionDegree != null && executionDegree.getExecutionYear() == executionYear
                    && degreeTypes.contains(executionDegree.getDegree().getDegreeType())) {
                return true;
            }
        }

        return false;

    }

    public ServiceAgreement getServiceAgreementFor(final ServiceAgreementTemplate serviceAgreementTemplate) {
        for (final ServiceAgreement serviceAgreement : getServiceAgreementsSet()) {
            if (serviceAgreement.getServiceAgreementTemplate() == serviceAgreementTemplate) {
                return serviceAgreement;
            }
        }
        return null;
    }

    public boolean hasServiceAgreementFor(final ServiceAgreementTemplate serviceAgreementTemplate) {
        return getServiceAgreementFor(serviceAgreementTemplate) != null;
    }

    public String getFirstAndLastName() {
        final String[] name = getName().split(" ");
        return name[0] + " " + name[name.length - 1];
    }

    public static Collection<Person> findPerson(final String name) {
        return findPerson(name, Integer.MAX_VALUE);
    }

    public static Collection<Person> findInternalPerson(final String name) {
        return findInternalPerson(name, Integer.MAX_VALUE);
    }

    public static Collection<Person> findInternalPerson(final String name, int maxHits) {
        return findInternalPersonStream(name, maxHits).collect(Collectors.toSet());
    }

    public static Collection<Person> findInternalPersonMatchingFirstAndLastName(final String completeName) {
        if (completeName != null) {
            final String[] splittedName = completeName.split(" ");
            return splittedName.length > 0 ? findInternalPerson(splittedName[0] + " " + splittedName[splittedName.length - 1]) : Collections.EMPTY_LIST;
        }
        return Collections.EMPTY_LIST;
    }

    public static Collection<Person> findExternalPerson(final String name) {
        return findExternalPerson(name, Integer.MAX_VALUE);
    }

    public static Collection<Person> findExternalPerson(final String name, int maxHits) {
        return findExternalPersonStream(name, maxHits).collect(Collectors.toSet());
    }

    public static Collection<Person> findPersonByDocumentID(final String documentIDValue) {
        final Collection<Person> people = new ArrayList<Person>();
        if (!StringUtils.isEmpty(documentIDValue)) {
            for (final IdDocument idDocument : IdDocument.find(documentIDValue)) {
                people.add(idDocument.getPerson());
            }
        }
        return people;
    }

    public static Person readPersonByEmailAddress(final String email) {
        final EmailAddress emailAddress = EmailAddress.find(email);
        return emailAddress != null && emailAddress.getParty().isPerson() ? (Person) emailAddress.getParty() : null;
    }

    public boolean hasEmailAddress(final String email) {
        for (final PartyContact partyContact : getPartyContactsSet()) {
            if (partyContact.isEmailAddress()) {
                final EmailAddress emailAddress = (EmailAddress) partyContact;
                if (emailAddress.hasValue(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getUnitText() {
        if (getEmployee() != null && getEmployee().getLastWorkingPlace() != null) {
            return getEmployee().getLastWorkingPlace().getNameWithAcronym();
        } else if (hasExternalContract()) {
            return getExternalContract().getInstitutionUnit().getPresentationNameWithParents();
        }
        return "";
    }

    public Set<Thesis> getOrientedOrCoorientedThesis(final ExecutionYear year) {
        final Set<Thesis> thesis = new HashSet<Thesis>();
        for (final ThesisEvaluationParticipant participant : getThesisEvaluationParticipantsSet()) {
            if (participant.getThesis().getEnrolment().getExecutionYear().equals(year)
                    && (participant.getType() == ThesisParticipationType.ORIENTATOR || participant.getType() == ThesisParticipationType.COORIENTATOR)) {
                thesis.add(participant.getThesis());
            }
        }
        return thesis;
    }

    public List<ThesisEvaluationParticipant> getThesisEvaluationParticipants(final ExecutionSemester executionSemester) {
        final ArrayList<ThesisEvaluationParticipant> participants = new ArrayList<ThesisEvaluationParticipant>();

        for (final ThesisEvaluationParticipant participant : this.getThesisEvaluationParticipantsSet()) {
            if (participant.getThesis().getEnrolment().getExecutionYear().equals(executionSemester.getExecutionYear())) {
                participants.add(participant);
            }
        }
        Collections.sort(participants, ThesisEvaluationParticipant.COMPARATOR_BY_STUDENT_NUMBER);
        return participants;
    }

    public List<ResearchUnit> getWorkingResearchUnits() {
        final List<ResearchUnit> units = new ArrayList<ResearchUnit>();
        final Collection<? extends Accountability> parentAccountabilities =
                getParentAccountabilities(AccountabilityTypeEnum.RESEARCH_CONTRACT);

        final YearMonthDay currentDate = new YearMonthDay();
        for (final Accountability accountability : parentAccountabilities) {
            if (accountability.isActive(currentDate)) {
                units.add((ResearchUnit) accountability.getParentParty());
            }
        }

        return units;
    }

    public List<ResearchUnit> getWorkingResearchUnitsAndParents() {
        final Set<ResearchUnit> baseUnits = new HashSet<ResearchUnit>();
        for (final ResearchUnit unit : getWorkingResearchUnits()) {
            baseUnits.add(unit);
            for (final Unit parentUnit : unit.getAllActiveParentUnits(new YearMonthDay())) {
                if (parentUnit.isResearchUnit()) {
                    baseUnits.add((ResearchUnit) parentUnit);
                }
            }
        }
        return new ArrayList<ResearchUnit>(baseUnits);
    }

    public Set<Unit> getAssociatedResearchOrDepartmentUnits() {
        final Set<Unit> units = new HashSet<Unit>();
        final Set<Accountability> parentAccountabilities = new HashSet<Accountability>();

        parentAccountabilities.addAll(getParentAccountabilities(AccountabilityTypeEnum.RESEARCH_CONTRACT));
        parentAccountabilities.addAll(getParentAccountabilities(AccountabilityTypeEnum.WORKING_CONTRACT));

        for (final Accountability accountability : parentAccountabilities) {
            final Unit unit = getActiveAncestorUnitFromAccountability(accountability);
            if (unit != null) {
                units.add(unit);
            }
        }

        return units;
    }

    private Unit getActiveAncestorUnitFromAccountability(final Accountability accountability) {
        final YearMonthDay currentDate = new YearMonthDay();
        if (!accountability.isActive(currentDate)) {
            return null;
        }

        final Unit parentUnit = (Unit) accountability.getParentParty();
        if (isResearchDepartmentScientificOrSectionUnitType(parentUnit)) {
            return parentUnit;
        }

        for (final Unit grandParentUnit : parentUnit.getParentUnits()) {
            if (isResearchDepartmentScientificOrSectionUnitType(grandParentUnit)) {
                return grandParentUnit;
            }
        }

        return null;
    }

    private boolean isResearchDepartmentScientificOrSectionUnitType(final Unit unit) {
        return unit.isResearchUnit() || unit.isDepartmentUnit() || unit.isScientificAreaUnit() || unit.isSectionUnit();
    }

    // FIXME Anil : This method is identical to getWorkingResearchUnitNames
    public String getAssociatedResearchOrDepartmentUnitsNames() {
        String names = "";
        final Set<Unit> units = getAssociatedResearchOrDepartmentUnits();
        int length = units.size();
        for (final Unit unit : units) {
            names += unit.getName();
            if (--length > 0) {
                names += ", ";
            }
        }
        return names;
    }

    public String getWorkingResearchUnitNames() {

        String names = "";
        final List<ResearchUnit> units = getWorkingResearchUnits();
        int length = units.size();
        for (final ResearchUnit unit : units) {
            names += unit.getName();
            if (--length > 0) {
                names += ", ";
            }
        }
        return names;
    }

    public boolean isExternalPerson() {
        return !hasActiveInternalContract() && (hasExternalContract() || hasExternalResearchContract());
    }

    private boolean hasActiveInternalContract() {
        final Collection<EmployeeContract> contracts =
                (Collection<EmployeeContract>) getParentAccountabilities(AccountabilityTypeEnum.WORKING_CONTRACT,
                        EmployeeContract.class);

        final YearMonthDay currentDate = new YearMonthDay();
        for (final EmployeeContract employeeContract : contracts) {
            if (employeeContract.isActive(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPhotoAvailableToCurrentUser() {
        return isPhotoAvailableToPerson(AccessControl.getPerson());
    }

    public boolean isPhotoAvailableToPerson(Person requester) {
        if (isPhotoPubliclyAvailable()) {
            return true;
        }
        return requester != null && requester.hasRole(RoleType.PERSON);
    }

    @Override
    public Photograph getPersonalPhoto() {
        Photograph photo = super.getPersonalPhoto();
        if (photo == null) {
            return null;
        }
        do {
            if (photo.getState() == PhotoState.APPROVED) {
                return photo;
            }
            photo = photo.getPrevious();
        } while (photo != null);
        return null;
    }

    public Photograph getPersonalPhotoEvenIfPending() {
        Photograph photo = super.getPersonalPhoto();
        if (photo == null) {
            return null;
        }

        do {
            if (photo.getState() != PhotoState.REJECTED && photo.getState() != PhotoState.USER_REJECTED) {
                return photo;
            }

            photo = photo.getPrevious();
        } while (photo != null);

        return null;
    }

    public Photograph getPersonalPhotoEvenIfRejected() {
        return super.getPersonalPhoto();
    }

    @Override
    public void setPersonalPhoto(final Photograph photo) {
        if (super.getPersonalPhoto() != null) {
            photo.setPrevious(super.getPersonalPhoto());
        }
        super.setPersonalPhoto(photo);
        if (photo != null) {
            photo.logCreate(this);
        }
    }

    public List<Photograph> getPhotographHistory() {
        final LinkedList<Photograph> history = new LinkedList<Photograph>();
        for (Photograph photo = super.getPersonalPhoto(); photo != null; photo = photo.getPrevious()) {
            history.addFirst(photo);
        }
        return history;
    }

    public boolean isPhotoPubliclyAvailable() {
        return getPhotoAvailable();
    }

    public boolean isDefaultEmailVisible() {
        return getDefaultEmailAddress() == null ? false : getDefaultEmailAddress().getVisibleToPublic();
    }

    public boolean isDefaultWebAddressVisible() {
        return getDefaultWebAddress() == null ? false : getDefaultWebAddress().getVisibleToPublic();
    }

    @Deprecated
    public Boolean getAvailableEmail() {
        return isDefaultEmailVisible();
    }

    @Deprecated
    public void setAvailableEmail(final Boolean available) {
        if (getDefaultEmailAddress() != null) {
            getDefaultEmailAddress().setVisibleToPublic(available);
        }
    }

    @Deprecated
    public Boolean getAvailableWebSite() {
        return isDefaultWebAddressVisible();
    }

    @Deprecated
    public void setAvailableWebSite(final Boolean available) {
        if (getDefaultWebAddress() != null) {
            getDefaultWebAddress().setVisibleToPublic(available);
        }
    }

    public List<UnitFile> getUploadedFiles(final Unit unit) {
        final List<UnitFile> files = new ArrayList<UnitFile>();
        for (final UnitFile file : getUploadedFilesSet()) {
            if (file.getUnit().equals(unit)) {
                files.add(file);
            }
        }
        return files;
    }

    public String getPresentationName() {
        final String username = getUsername();
        return username == null ? getName() : getName() + " (" + getUsername() + ")";
    }

    @Override
    public String getPartyPresentationName() {
        return getPresentationName();
    }

    public PersonFunction getActiveGGAEDelegatePersonFunction() {
        for (final PersonFunction personFunction : getActivePersonFunctions()) {
            if (personFunction.getFunction().getFunctionType().equals(FunctionType.DELEGATE_OF_GGAE)) {
                return personFunction;
            }
        }
        return null;
    }

    public List<PersonFunction> getAllGGAEDelegatePersonFunctions() {
        final List<PersonFunction> result = new ArrayList<PersonFunction>();
        for (final PersonFunction personFunction : getPersonFunctions()) {
            if (personFunction.getFunction().getFunctionType().equals(FunctionType.DELEGATE_OF_GGAE)) {
                result.add(personFunction);
            }
        }
        return result;
    }

    public boolean isPedagogicalCouncilMember() {
        return hasRole(RoleType.PEDAGOGICAL_COUNCIL);
    }

    public Collection<Forum> getForuns(final ExecutionSemester executionSemester) {
        final Collection<Forum> foruns = new HashSet<Forum>();
        if (getTeacher() != null) {
            foruns.addAll(getTeacher().getForuns(executionSemester));
        }

        if (getStudent() != null) {
            foruns.addAll(getStudent().getForuns(executionSemester));
        }

        for (final ForumSubscription forumSubscription : getForumSubscriptionsSet()) {
            foruns.add(forumSubscription.getForum());
        }

        return foruns;
    }

    private boolean hasValidIndividualCandidacy(final Class<? extends IndividualCandidacy> clazz,
            final ExecutionInterval executionInterval) {
        for (final IndividualCandidacyPersonalDetails candidacyDetails : getIndividualCandidaciesSet()) {
            final IndividualCandidacy candidacy = candidacyDetails.getCandidacy();
            if (!candidacy.isCancelled() && candidacy.getClass().equals(clazz) && candidacy.isFor(executionInterval)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasValidOver23IndividualCandidacy(final ExecutionInterval executionInterval) {
        return hasValidIndividualCandidacy(Over23IndividualCandidacy.class, executionInterval);
    }

    public boolean hasValidSecondCycleIndividualCandidacy(final ExecutionInterval executionInterval) {
        return hasValidIndividualCandidacy(SecondCycleIndividualCandidacy.class, executionInterval);
    }

    public boolean hasValidDegreeCandidacyForGraduatedPerson(final ExecutionInterval executionInterval) {
        return hasValidIndividualCandidacy(DegreeCandidacyForGraduatedPerson.class, executionInterval);
    }

    public boolean hasValidStandaloneIndividualCandidacy(final ExecutionInterval executionInterval) {
        return hasValidIndividualCandidacy(StandaloneIndividualCandidacy.class, executionInterval);
    }

    public List<Formation> getFormations() {
        final List<Formation> formations = new ArrayList<Formation>();
        for (final Qualification qualification : getAssociatedQualificationsSet()) {
            if (qualification instanceof Formation) {
                formations.add((Formation) qualification);
            }
        }
        return formations;
    }

    public Qualification getLastQualification() {
        return !getAssociatedQualificationsSet().isEmpty() ? Collections.max(getAssociatedQualificationsSet(),
                Qualification.COMPARATOR_BY_YEAR) : null;
    }

    public boolean hasGratuityOrAdministrativeOfficeFeeAndInsuranceDebtsFor(final ExecutionYear executionYear) {
        for (final AnnualEvent annualEvent : getAnnualEventsFor(executionYear)) {
            if (annualEvent instanceof GratuityEvent || annualEvent instanceof AdministrativeOfficeFeeAndInsuranceEvent) {
                if (annualEvent.isOpen()) {
                    return true;
                }
            }
        }

        return false;

    }

    public Set<AnnualIRSDeclarationDocument> getAnnualIRSDocuments() {
        final Set<AnnualIRSDeclarationDocument> result = new HashSet<AnnualIRSDeclarationDocument>();

        for (final GeneratedDocument each : getAddressedDocumentSet()) {
            if (each instanceof AnnualIRSDeclarationDocument) {
                result.add((AnnualIRSDeclarationDocument) each);
            }
        }

        return result;
    }

    public AnnualIRSDeclarationDocument getAnnualIRSDocumentFor(final Integer year) {
        for (final AnnualIRSDeclarationDocument each : getAnnualIRSDocuments()) {
            if (each.getYear().compareTo(year) == 0) {
                return each;
            }
        }

        return null;

    }

    public boolean hasAnnualIRSDocumentFor(final Integer year) {
        return getAnnualIRSDocumentFor(year) != null;
    }

    public Person getIncompatibleVigilantPerson() {
        return getIncompatiblePerson() != null ? getIncompatiblePerson() : getIncompatibleVigilant();
    }

    public void setIncompatibleVigilantPerson(final Person person) {
        setIncompatibleVigilant(person);
        setIncompatiblePerson(null);
    }

    public void removeIncompatibleVigilantPerson() {
        setIncompatibleVigilant(null);
        setIncompatiblePerson(null);
    }

    public List<UnavailablePeriod> getUnavailablePeriodsForGivenYear(final ExecutionYear executionYear) {
        final Collection<UnavailablePeriod> unavailablePeriods = this.getUnavailablePeriodsSet();
        final List<UnavailablePeriod> unavailablePeriodsForGivenYear = new ArrayList<UnavailablePeriod>();
        for (final UnavailablePeriod unavailablePeriod : unavailablePeriods) {
            if (unavailablePeriod.getBeginDate().getYear() == executionYear.getBeginCivilYear()
                    || unavailablePeriod.getBeginDate().getYear() == executionYear.getEndCivilYear()) {
                unavailablePeriodsForGivenYear.add(unavailablePeriod);
            }
        }
        return unavailablePeriodsForGivenYear;
    }

    public boolean hasAnyAdministrativeOfficeFeeAndInsuranceEventInDebt() {
        for (final Event event : getEventsByEventType(EventType.ADMINISTRATIVE_OFFICE_FEE_INSURANCE)) {
            if (event.isInDebt()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAnyPastAdministrativeOfficeFeeAndInsuranceEventInDebt() {
        for (final Event event : getEventsByEventType(EventType.ADMINISTRATIVE_OFFICE_FEE_INSURANCE)) {
            final AdministrativeOfficeFeeAndInsuranceEvent administrativeOfficeFeeAndInsuranceEvent =
                    (AdministrativeOfficeFeeAndInsuranceEvent) event;

            if (administrativeOfficeFeeAndInsuranceEvent instanceof PastAdministrativeOfficeFeeAndInsuranceEvent) {
                if (event.isInDebt()) {
                    return true;
                }
            }

        }

        return false;
    }

    public boolean hasAnyResidencePaymentsInDebtForPreviousYear() {
        final int previousYear = new LocalDate().minusYears(1).getYear();

        for (final Event event : getResidencePaymentEvents()) {
            final ResidenceEvent residenceEvent = (ResidenceEvent) event;
            if (residenceEvent.isFor(previousYear) && !residenceEvent.isCancelled() && !residenceEvent.isPayed()) {
                return true;
            }
        }
        return false;
    }

    public Professorship getProfessorshipByExecutionCourse(final ExecutionCourse executionCourse) {
        return (Professorship) CollectionUtils.find(getProfessorshipsSet(), new Predicate() {
            @Override
            public boolean evaluate(final Object arg0) {
                final Professorship professorship = (Professorship) arg0;
                return professorship.getExecutionCourse() == executionCourse;
            }
        });
    }

    public List<Professorship> getProfessorshipsByExecutionSemester(final ExecutionSemester executionSemester) {
        final List<Professorship> professorships = new ArrayList<Professorship>();
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (professorship.getExecutionCourse().getExecutionPeriod() == executionSemester) {
                professorships.add(professorship);
            }
        }
        return professorships;
    }

    public void updateResponsabilitiesFor(final String executionYearId, final List<String> executionCourses)
            throws MaxResponsibleForExceed, InvalidCategory {

        if (executionYearId == null || executionCourses == null) {
            throw new NullPointerException();
        }

        boolean responsible;
        for (final Professorship professorship : this.getProfessorshipsSet()) {
            final ExecutionCourse executionCourse = professorship.getExecutionCourse();
            if (executionCourse.getExecutionPeriod().getExecutionYear().getExternalId().equals(executionYearId)) {
                responsible = executionCourses.contains(executionCourse.getExternalId());
                if (!professorship.getResponsibleFor().equals(Boolean.valueOf(responsible)) && this.getTeacher() != null) {
                    ResponsibleForValidator.getInstance().validateResponsibleForList(this.getTeacher(), executionCourse,
                            professorship);
                    professorship.setResponsibleFor(responsible);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Professorship> getResponsableProfessorships() {
        final List<Professorship> result = new ArrayList<Professorship>();
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (professorship.isResponsibleFor()) {
                result.add(professorship);
            }
        }
        return result;
    }

    public boolean hasProfessorshipForExecutionCourse(final ExecutionCourse executionCourse) {
        return getProfessorshipByExecutionCourse(executionCourse) != null;
    }

    public Set<PhdAlertMessage> getUnreadedPhdAlertMessages() {
        final Set<PhdAlertMessage> result = new HashSet<PhdAlertMessage>();

        for (final PhdAlertMessage message : getPhdAlertMessagesSet()) {
            if (!message.isReaded()) {
                result.add(message);
            }
        }

        return result;
    }

    public boolean isPhdStudent() {
        return !getPhdIndividualProgramProcessesSet().isEmpty();
    }

    public RegistrationProtocol getOnlyRegistrationProtocol() {
        if (getRegistrationProtocolsSet().size() == 1) {
            return getRegistrationProtocolsSet().iterator().next();
        }
        return null;
    }

    @Atomic
    public void transferEventsAndAccounts(final Person sourcePerson) {
        if (!AccessControl.getPerson().hasRole(RoleType.MANAGER)) {
            throw new DomainException("permission.denied");
        }

        if (sourcePerson.getInternalAccount() != null) {
            for (final Entry entry : sourcePerson.getInternalAccount().getEntriesSet()) {
                this.getInternalAccount().transferEntry(entry);
                this.getEventsSet().add(entry.getAccountingTransaction().getEvent());
            }

        }

        if (sourcePerson.getExternalAccount() != null) {
            for (final Entry entry : sourcePerson.getExternalAccount().getEntriesSet()) {
                this.getExternalAccount().transferEntry(entry);
                this.getEventsSet().add(entry.getAccountingTransaction().getEvent());
            }
        }
    }

    public Professorship isResponsibleFor(final ExecutionCourse executionCourse) {
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (professorship.getResponsibleFor() && professorship.getExecutionCourse() == executionCourse) {
                return professorship;
            }
        }
        return null;
    }

    public List<Professorship> getProfessorships(final ExecutionSemester executionSemester) {
        final List<Professorship> professorships = new ArrayList<Professorship>();
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (professorship.getExecutionCourse().getExecutionPeriod().equals(executionSemester)) {
                professorships.add(professorship);
            }
        }
        return professorships;
    }

    public List<Professorship> getProfessorships(final ExecutionYear executionYear) {
        final List<Professorship> professorships = new ArrayList<Professorship>();
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (professorship.getExecutionCourse().getExecutionPeriod().getExecutionYear().equals(executionYear)) {
                professorships.add(professorship);
            }
        }
        return professorships;
    }

    public boolean teachesAny(final Collection<ExecutionCourse> executionCourses) {
        for (final Professorship professorship : getProfessorshipsSet()) {
            if (executionCourses.contains(professorship.getExecutionCourse())) {
                return true;
            }
        }
        return false;
    }

    public boolean isTeacherEvaluationCoordinatorCouncilMember() {
        PersistentGroup group = Bennu.getInstance().getTeacherEvaluationCoordinatorCouncil();
        return group != null ? group.isMember(Authenticate.getUser()) : false;
    }

    public EmailAddress getEmailAddressForSendingEmails() {
        final Boolean disableSendEmails = getDisableSendEmails();
        if (disableSendEmails != null && disableSendEmails.booleanValue()) {
            return null;
        }
        final EmailAddress defaultEmailAddress = getDefaultEmailAddress();
        if (defaultEmailAddress != null) {
            return defaultEmailAddress;
        }
        final EmailAddress institutionalEmailAddress = getInstitutionalEmailAddress();
        if (institutionalEmailAddress != null) {
            return institutionalEmailAddress;
        }
        for (final PartyContact partyContact : getPartyContactsSet()) {
            if (partyContact.isEmailAddress() && partyContact.isActiveAndValid() && partyContact.isValid()) {
                final EmailAddress otherEmailAddress = (EmailAddress) partyContact;
                return otherEmailAddress;
            }
        }
        return null;
    }

    public String getEmailForSendingEmails() {
        final EmailAddress emailAddress = getEmailAddressForSendingEmails();
        return emailAddress == null ? null : emailAddress.getValue();
    }

    public String getEmployer(final RoleType roleType) {
        final PersonProfessionalData personProfessionalData = getPersonProfessionalData();
        return personProfessionalData == null ? null : personProfessionalData.getEmployer(roleType);
    }

    public String getWorkingPlaceCostCenter() {
        final Employee employee = getEmployee();
        final Unit unit = employee == null ? null : employee.getCurrentWorkingPlace();
        final Integer costCenterCode = unit == null ? null : unit.getCostCenterCode();
        return costCenterCode == null ? null : costCenterCode.toString();
    }

    public String getEmployeeRoleDescription() {
        final RoleType roleType =
                getMostImportantRoleType(RoleType.TEACHER, RoleType.RESEARCHER, RoleType.EMPLOYEE, RoleType.GRANT_OWNER);
        if (roleType == RoleType.RESEARCHER && !hasRole(RoleType.EMPLOYEE)) {
            return "EXTERNAL_RESEARCH_PERSONNEL";
        }
        return roleType == null ? null : roleType.name();
    }

    // Temp method used for mission system.
    public String getWorkingPlaceForAnyRoleType() {
        final Unit unit = getWorkingPlaceUnitForAnyRoleType();
        return unit != null ? unit.getCostCenterCode().toString() : null;
    }

    public Unit getWorkingPlaceUnitForAnyRoleType() {
        if (hasRole(RoleType.TEACHER) || hasRole(RoleType.EMPLOYEE) || hasRole(RoleType.GRANT_OWNER)) {
            return getEmployee() != null ? getEmployee().getCurrentWorkingPlace() : null;
        }
        if (hasRole(RoleType.RESEARCHER)) {
            if (getEmployee() != null && getResearcher() != null && getResearcher().isActiveContractedResearcher()) {
                final Unit currentWorkingPlace = getEmployee().getCurrentWorkingPlace();
                if (currentWorkingPlace != null) {
                    return currentWorkingPlace;
                }
            }
            final Collection<? extends Accountability> accountabilities =
                    getParentAccountabilities(AccountabilityTypeEnum.RESEARCH_CONTRACT);
            final YearMonthDay currentDate = new YearMonthDay();
            for (final Accountability accountability : accountabilities) {
                if (accountability.isActive(currentDate)) {
                    return (Unit) accountability.getParentParty();
                }
            }
        }
        return null;
    }

    private RoleType getMostImportantRoleType(final RoleType... roleTypes) {
        for (final RoleType roleType : roleTypes) {
            if (hasRole(roleType)) {
                return roleType;
            }
        }
        return null;
    }

    private boolean hasAnyRoleHack(final RoleType[] roleTypes) {
        for (final RoleType roleType : roleTypes) {
            if (hasRole(roleType)
                    && (roleType != RoleType.RESEARCHER || getResearcher() != null
                            && getResearcher().isActiveContractedResearcher())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyRole(final RoleType[] roleTypes) {
        for (final RoleType roleType : roleTypes) {
            if (hasRole(roleType)) {
                return true;
            }
        }
        return false;
    }

    public boolean areContactsRecent(final Class<? extends PartyContact> contactClass, final int daysNotUpdated) {
        final List<? extends PartyContact> partyContacts = getPartyContacts(contactClass);
        boolean isUpdated = false;
        for (final PartyContact partyContact : partyContacts) {
            if (partyContact.getLastModifiedDate() == null) {
                isUpdated = isUpdated || false;
            } else {
                final DateTime lastModifiedDate = partyContact.getLastModifiedDate();
                final DateTime now = new DateTime();
                final Months months = Months.monthsBetween(lastModifiedDate, now);
                if (months.getMonths() > daysNotUpdated) {
                    isUpdated = isUpdated || false;
                } else {
                    isUpdated = isUpdated || true;
                }
            }
        }
        return isUpdated;
    }

    /**
     * Use socialSecurityNumber instead
     */
    @Override
    @Deprecated
    public String getFiscalCode() {
        return super.getFiscalCode();
    }

    @Override
    @Deprecated
    public void setFiscalCode(final String value) {
        super.setFiscalCode(value);
    }

    @Deprecated
    public final boolean namesCorrectlyPartitioned() {
        if (StringUtils.isEmpty(getGivenNames()) && StringUtils.isEmpty(getFamilyNames())) {
            return true;
        }
        if (StringUtils.isEmpty(getGivenNames())) {
            return false;
        }

        final String fullName = getName();
        final String familyName = getFamilyNames();
        final String composedName =
                familyName == null || familyName.isEmpty() ? getGivenNames() : getGivenNames() + " " + familyName;

        return fullName.equals(composedName);
    }

    public static Person findByUsername(final String username) {
        final User user = User.findByUsername(username);
        return user == null ? null : user.getPerson();
    }

    public String getIdentificationDocumentExtraDigitValue() {
        final PersonIdentificationDocumentExtraInfo result =
                getPersonIdentificationDocumentExtraInfo(IdentificationDocumentExtraDigit.class);
        return result != null ? result.getValue() : null;
    }

    public String getIdentificationDocumentSeriesNumberValue() {
        final PersonIdentificationDocumentExtraInfo result =
                getPersonIdentificationDocumentExtraInfo(IdentificationDocumentSeriesNumber.class);
        return result != null ? result.getValue() : null;
    }

    public PersonIdentificationDocumentExtraInfo getPersonIdentificationDocumentExtraInfo(final Class clazz) {
        PersonIdentificationDocumentExtraInfo result = null;
        for (final PersonIdentificationDocumentExtraInfo info : getPersonIdentificationDocumentExtraInfoSet()) {
            if (info.getClass() == clazz
                    && (result == null || result.getRegisteredInSystemTimestamp().isBefore(info.getRegisteredInSystemTimestamp()))) {
                result = info;
            }
        }
        return result == null ? null : result;
    }

    public void setIdentificationDocumentSeriesNumber(final String identificationDocumentSeriesNumber) {
        if (!StringUtils.isEmpty(identificationDocumentSeriesNumber)) {
            if (identificationDocumentSeriesNumber.trim().length() == 1) {
                final PersonIdentificationDocumentExtraInfo personIdentificationDocumentExtraInfo =
                        getPersonIdentificationDocumentExtraInfo(IdentificationDocumentExtraDigit.class);
                if (personIdentificationDocumentExtraInfo == null) {
                    new IdentificationDocumentExtraDigit(this, identificationDocumentSeriesNumber);
                } else {
                    personIdentificationDocumentExtraInfo.setValue(identificationDocumentSeriesNumber);
                }
            } else {
                final PersonIdentificationDocumentExtraInfo personIdentificationDocumentExtraInfo =
                        getPersonIdentificationDocumentExtraInfo(IdentificationDocumentSeriesNumber.class);
                if (personIdentificationDocumentExtraInfo == null) {
                    new IdentificationDocumentSeriesNumber(this, identificationDocumentSeriesNumber);
                } else {
                    personIdentificationDocumentExtraInfo.setValue(identificationDocumentSeriesNumber);
                }
            }
        }
    }

    public void setIdentificationDocumentExtraDigit(final String identificationDocumentExtraDigit) {
        if (!StringUtils.isEmpty(identificationDocumentExtraDigit)) {
            final PersonIdentificationDocumentExtraInfo personIdentificationDocumentExtraInfo =
                    getPersonIdentificationDocumentExtraInfo(IdentificationDocumentExtraDigit.class);
            if (personIdentificationDocumentExtraInfo == null) {
                new IdentificationDocumentExtraDigit(this, identificationDocumentExtraDigit);
            } else {
                personIdentificationDocumentExtraInfo.setValue(identificationDocumentExtraDigit);
            }
        }
    }

    @Override
    @Atomic
    public void setNumberOfValidationRequests(final Integer numberOfValidationRequests) {
        super.setNumberOfValidationRequests(numberOfValidationRequests);
    }

    public boolean getCanValidateContacts() {
        final DateTime now = new DateTime();
        final DateTime requestDate = getLastValidationRequestDate();
        if (requestDate == null || getNumberOfValidationRequests() == null) {
            return true;
        }
        final DateTime plus30 = requestDate.plusDays(30);
        if (now.isAfter(plus30) || now.isEqual(plus30)) {
            setNumberOfValidationRequests(0);
        }
        return getNumberOfValidationRequests() <= MAX_VALIDATION_REQUESTS;
    }

    @Atomic
    public void incValidationRequest() {
        getCanValidateContacts();
        Integer numberOfValidationRequests = getNumberOfValidationRequests();
        numberOfValidationRequests = numberOfValidationRequests == null ? 0 : numberOfValidationRequests;
        if (numberOfValidationRequests <= MAX_VALIDATION_REQUESTS) {
            setNumberOfValidationRequests(numberOfValidationRequests + 1);
            setLastValidationRequestDate(new DateTime());
        }
    }

    @Override
    public Integer getNumberOfValidationRequests() {
        final Integer numberOfValidationRequests = super.getNumberOfValidationRequests();
        if (numberOfValidationRequests == null) {
            return 0;
        }
        return numberOfValidationRequests;
    }

    public boolean isOptOutAvailable() {
        // TODO Properly implement this
        return false;
    }

    @Deprecated
    public java.util.Date getDateOfBirth() {
        final org.joda.time.YearMonthDay ymd = getDateOfBirthYearMonthDay();
        return ymd == null ? null : new java.util.Date(ymd.getYear() - 1900, ymd.getMonthOfYear() - 1, ymd.getDayOfMonth());
    }

    @Deprecated
    public void setDateOfBirth(final java.util.Date date) {
        if (date == null) {
            setDateOfBirthYearMonthDay(null);
        } else {
            setDateOfBirthYearMonthDay(org.joda.time.YearMonthDay.fromDateFields(date));
        }
    }

    @Deprecated
    public java.util.Date getEmissionDateOfDocumentId() {
        final org.joda.time.YearMonthDay ymd = getEmissionDateOfDocumentIdYearMonthDay();
        return ymd == null ? null : new java.util.Date(ymd.getYear() - 1900, ymd.getMonthOfYear() - 1, ymd.getDayOfMonth());
    }

    @Deprecated
    public void setEmissionDateOfDocumentId(final java.util.Date date) {
        if (date == null) {
            setEmissionDateOfDocumentIdYearMonthDay(null);
        } else {
            setEmissionDateOfDocumentIdYearMonthDay(org.joda.time.YearMonthDay.fromDateFields(date));
        }
    }

    @Deprecated
    public java.util.Date getExpirationDateOfDocumentId() {
        final org.joda.time.YearMonthDay ymd = getExpirationDateOfDocumentIdYearMonthDay();
        return ymd == null ? null : new java.util.Date(ymd.getYear() - 1900, ymd.getMonthOfYear() - 1, ymd.getDayOfMonth());
    }

    @Deprecated
    public void setExpirationDateOfDocumentId(final java.util.Date date) {
        if (date == null) {
            setExpirationDateOfDocumentIdYearMonthDay(null);
        } else {
            setExpirationDateOfDocumentIdYearMonthDay(org.joda.time.YearMonthDay.fromDateFields(date));
        }
    }

    @Deprecated
    public static String readAllEmails() {
        final StringBuilder builder = new StringBuilder();
        for (final Party party : Bennu.getInstance().getPartysSet()) {
            if (party.isPerson()) {
                final Person person = (Person) party;
                final String email = person.getEmailForSendingEmails();
                if (email != null) {
                    final User user = person.getUser();
                    if (user != null) {
                        final String username = user.getUsername();
                        builder.append(username);
                        builder.append("\t");
                        builder.append(email);
                        builder.append("\n");
                    }
                }
            }
        }
        return builder.toString();
    }

    public static String readAllUserData(final String types) {
        RoleType[] roles;
        if (types != null && StringUtils.isNotBlank(types)) {
            roles = new RoleType[types.split("-").length];
            int i = 0;
            for (final String typeString : types.split("-")) {
                roles[i] = RoleType.valueOf(typeString);
                i++;
            }
        } else {
            roles = new RoleType[0];
        }
        final StringBuilder builder = new StringBuilder();
        for (final User user : Bennu.getInstance().getUserSet()) {
            if (!StringUtils.isEmpty(user.getUsername())) {
                final Person person = user.getPerson();
                if (roles.length == 0 || person.hasAnyRole(roles)) {
                    builder.append(user.getUsername());
                    builder.append("\t");
                    builder.append(person.getName());
                    builder.append("\t");
                    builder.append(person.getExternalId());
                    builder.append("\n");
                }
            }
        }
        return builder.toString();
    }

    /*********************************
     * LOGGING METHODS AND OVERRIDES *
     ********************************/

    private void logSetter(String keyTypeOfData, String oldValue, String newValue, String keyLabel) {

        final String personViewed = PersonInformationLog.getPersonNameForLogDescription(this);
        if (oldValue.compareTo(newValue) != 0) {
            String infoLabel = BundleUtil.getString(Bundle.APPLICATION, keyLabel);
            String typeOfData = BundleUtil.getString(Bundle.MESSAGING, keyTypeOfData);
            PersonInformationLog.createLog(this, Bundle.MESSAGING, "log.personInformation.edit.generalTemplate", typeOfData,
                    infoLabel, personViewed, oldValue);
        }
    }

    private void logSetterNullString(String keyInfoType, String oldValue, String newValue, String keyLabel) {
        String argNew, argOld;
        argOld = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.APPLICATION, "label.empty"), oldValue);
        argNew = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.APPLICATION, "label.empty"), newValue);
        logSetter(keyInfoType, argOld, argNew, keyLabel);
    }

    private void logSetterNullYearMonthDay(String keyInfoType, YearMonthDay oldValue, YearMonthDay newValue, String keyLabel) {
        Object argNew, argOld;
        String strNew, strOld;
        argOld = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.HTML, "text.dateEmpty"), oldValue);
        argNew = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.HTML, "text.dateEmpty"), newValue);

        if (argOld instanceof YearMonthDay) {
            strOld = ((YearMonthDay) argOld).toString("yyyy/MM/dd");
        } else {
            strOld = (String) argOld;
        }

        if (argNew instanceof YearMonthDay) {
            strNew = ((YearMonthDay) argNew).toString("yyyy/MM/dd");
        } else {
            strNew = (String) argNew;
        }
        logSetter(keyInfoType, strOld, strNew, keyLabel);
    }

    private void logSetterNullEnum(String keyInfoType, IPresentableEnum oldValue, IPresentableEnum newValue, String keyLabel) {
        Object argNew, argOld;
        String strNew, strOld;
        argOld = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.APPLICATION, "label.empty"), oldValue);
        argNew = valueToUpdateIfNewNotNull(BundleUtil.getString(Bundle.APPLICATION, "label.empty"), newValue);

        if (argOld instanceof Enum) {
            strOld = ((IPresentableEnum) argOld).getLocalizedName();
        } else {
            strOld = (String) argOld;
        }

        if (argNew instanceof Enum) {
            strNew = ((IPresentableEnum) argNew).getLocalizedName();
        } else {
            strNew = (String) argNew;
        }
        logSetter(keyInfoType, strOld, strNew, keyLabel);
    }

    @Override
    public void setGender(Gender arg) {
        logSetterNullEnum("log.personInformation.edit.generalTemplate.personalData", getGender(), arg, "label.gender");
        super.setGender(arg);
    }

    @Override
    public void setProfession(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.personalData", getProfession(), arg, "label.occupation");
        super.setProfession(arg);
    }

    @Override
    public void setMaritalStatus(MaritalStatus arg) {
        // avmc: logic here is different: null value is converted to UNKNOWN
        MaritalStatus argToSet;
        if (arg != null) {
            argToSet = arg;
        } else {
            argToSet = MaritalStatus.UNKNOWN;
        }
        logSetterNullEnum("log.personInformation.edit.generalTemplate.personalData", getMaritalStatus(), argToSet,
                "label.maritalStatus");
        super.setMaritalStatus(argToSet);
    }

    @Override
    public void setEmissionLocationOfDocumentId(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.personalId", getEmissionLocationOfDocumentId(), arg,
                "label.documentIdEmissionLocation");
        super.setEmissionLocationOfDocumentId(arg);
    }

    @Override
    public void setEmissionDateOfDocumentIdYearMonthDay(YearMonthDay arg) {
        logSetterNullYearMonthDay("log.personInformation.edit.generalTemplate.personalId",
                getEmissionDateOfDocumentIdYearMonthDay(), arg, "label.documentIdEmissionDate");
        super.setEmissionDateOfDocumentIdYearMonthDay(arg);
    }

    @Override
    public void setExpirationDateOfDocumentIdYearMonthDay(YearMonthDay arg) {
        logSetterNullYearMonthDay("log.personInformation.edit.generalTemplate.personalId",
                getExpirationDateOfDocumentIdYearMonthDay(), arg, "label.documentIdExpirationDate");
        super.setExpirationDateOfDocumentIdYearMonthDay(arg);
    }

    @Override
    public void setSocialSecurityNumber(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.personalId", getSocialSecurityNumber(), arg,
                "label.socialSecurityNumber");
        super.setSocialSecurityNumber(arg);
    }

    @Override
    public void setEidentifier(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.personalId", getEidentifier(), arg, "label.eidentifier");
        super.setEidentifier(arg);
    }

    @Override
    public void setDateOfBirthYearMonthDay(YearMonthDay arg) {
        logSetterNullYearMonthDay("log.personInformation.edit.generalTemplate.filiation", getDateOfBirthYearMonthDay(), arg,
                "label.dateOfBirth");
        super.setDateOfBirthYearMonthDay(arg);
    }

    // Nationality
    @Override
    public void setCountry(Country arg) {
        String argNew, argOld;

        if (getCountry() != null) {
            if (getCountry().getCountryNationality() != null) {
                argOld = getCountry().getCountryNationality().getContent();
            } else {
                argOld = getCountry().getName();
            }
        } else {
            argOld = BundleUtil.getString(Bundle.APPLICATION, "label.empty");
        }

        if (arg != null) {
            if (arg.getCountryNationality() != null) {
                argNew = arg.getCountryNationality().getContent();
            } else {
                argNew = arg.getName();
            }
        } else {
            argNew = BundleUtil.getString(Bundle.APPLICATION, "label.empty");
        }
        super.setCountry(arg);
        logSetter("log.personInformation.edit.generalTemplate.filiation", argOld, argNew, "label.nationality");
    }

    @Override
    public void setParishOfBirth(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.filiation", getParishOfBirth(), arg,
                "label.parishOfBirth");
        super.setParishOfBirth(arg);
    }

    @Override
    public void setDistrictSubdivisionOfBirth(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.filiation", getDistrictSubdivisionOfBirth(), arg,
                "label.districtSubdivisionOfBirth");
        super.setDistrictSubdivisionOfBirth(arg);
    }

    @Override
    public void setDistrictOfBirth(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.filiation", getDistrictOfBirth(), arg,
                "label.districtOfBirth");
        super.setDistrictOfBirth(arg);
    }

    // Not to be confused with Nationality
    @Override
    public void setCountryOfBirth(Country arg) {
        String argNew, argOld;

        if (getCountryOfBirth() != null) {
            argOld = getCountryOfBirth().getName();
        } else {
            argOld = BundleUtil.getString(Bundle.APPLICATION, "label.empty");
        }

        if (arg != null) {
            argNew = arg.getName();
        } else {
            argNew = BundleUtil.getString(Bundle.APPLICATION, "label.empty");
        }
        super.setCountryOfBirth(arg);
        logSetter("log.personInformation.edit.generalTemplate.filiation", argOld, argNew, "label.countryOfBirth");
    }

    @Override
    public void setNameOfMother(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.filiation", getNameOfMother(), arg, "label.nameOfMother");
        super.setNameOfMother(arg);
    }

    @Override
    public void setNameOfFather(String arg) {
        logSetterNullString("log.personInformation.edit.generalTemplate.filiation", getNameOfFather(), arg, "label.nameOfFather");
        super.setNameOfFather(arg);
    }

    @Override
    public void logCreateContact(PartyContact contact) {
        contact.logCreate(this);
    }

    @Override
    public void logEditContact(PartyContact contact, boolean propertiesChanged, boolean valueChanged, boolean createdNewContact,
            String newValue) {
        contact.logEdit(this, propertiesChanged, valueChanged, createdNewContact, newValue);
    }

    @Override
    public void logDeleteContact(PartyContact contact) {
        contact.logDelete(this);
    }

    @Override
    public void logValidContact(PartyContact contact) {
        contact.logValid(this);
    }

    @Override
    public void logRefuseContact(PartyContact contact) {
        contact.logRefuse(this);
    }

    public static Set<User> convertToUsers(Iterable<Person> persons) {
        return FluentIterable.from(persons).filter(new com.google.common.base.Predicate<Person>() {
            @Override
            public boolean apply(Person person) {
                return person.getUser() != null;
            }
        }).transform(personToUser).toSet();
    }

    public void ensureUserProfile() {
        if (getProfile() == null) {
            String givenNames = super.getGivenNames();
            String familyNames = super.getFamilyNames();
            if (Strings.isNullOrEmpty(givenNames) && Strings.isNullOrEmpty(familyNames) && !Strings.isNullOrEmpty(getName())) {
                HumanName name = HumanName.decompose(getName(), false);
                givenNames = name.getGivenNames();
                familyNames = name.getFamilyNames();
            }
            String displayName = super.getNickname();
            if (displayName != null && !HumanName.namesMatch(givenNames + " " + familyNames, displayName)) {
                displayName = null;
            }
            UserProfile profile = new UserProfile(givenNames, familyNames, displayName, getEmailForSendingEmails(), null);
            setProfile(profile);
            if (getUser() != null) {
                getUser().setProfile(profile);
                profile.setAvatarUrl(CoreConfiguration.getConfiguration().applicationUrl() + "/user/photo/"
                        + getUser().getUsername());
            }
        }
    }
}
