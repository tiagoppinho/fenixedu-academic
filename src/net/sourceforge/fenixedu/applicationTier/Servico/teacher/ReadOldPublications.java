/*
 * Created on 13/Nov/2003
 *  
 */
package net.sourceforge.fenixedu.applicationTier.Servico.teacher;

import java.util.List;

import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.dataTransferObject.InfoTeacher;
import net.sourceforge.fenixedu.dataTransferObject.SiteView;
import net.sourceforge.fenixedu.dataTransferObject.teacher.InfoOldPublication;
import net.sourceforge.fenixedu.dataTransferObject.teacher.InfoSiteOldPublications;
import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.teacher.OldPublication;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentTeacher;
import net.sourceforge.fenixedu.persistenceTier.teacher.IPersistentOldPublication;
import net.sourceforge.fenixedu.util.OldPublicationType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

/**
 * @author Leonor Almeida
 * @author Sergio Montelobo
 * 
 */
public class ReadOldPublications extends Service {

    public SiteView run(OldPublicationType oldPublicationType, String user) throws ExcepcaoPersistencia {
        IPersistentTeacher persistentTeacher = persistentSupport.getIPersistentTeacher();
        Teacher teacher = persistentTeacher.readTeacherByUsername(user);
        InfoTeacher infoTeacher = InfoTeacher.newInfoFromDomain(teacher);

        IPersistentOldPublication persistentOldPublication = persistentSupport
                .getIPersistentOldPublication();
        List publications = persistentOldPublication.readAllByTeacherIdAndOldPublicationType(teacher.getIdInternal(),
                oldPublicationType);

        List result = (List) CollectionUtils.collect(publications, new Transformer() {
            public Object transform(Object o) {
                OldPublication oldPublication = (OldPublication) o;
                return InfoOldPublication.newInfoFromDomain(oldPublication);
            }
        });

        InfoSiteOldPublications bodyComponent = new InfoSiteOldPublications();
        bodyComponent.setInfoOldPublications(result);
        bodyComponent.setOldPublicationType(oldPublicationType);
        bodyComponent.setInfoTeacher(infoTeacher);

        SiteView siteView = new SiteView(bodyComponent);
        return siteView;
    }

}