package app.owlcms.ui.preparation;

import java.time.LocalDate;
import java.util.Collection;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Validator;

import app.owlcms.components.fields.LocalDateField;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.competition.CompetitionRepository;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.ui.crudui.OwlcmsCrudFormFactory;
import ch.qos.logback.classic.Logger;

@SuppressWarnings("serial")
class CompetitionEditingFormFactory extends OwlcmsCrudFormFactory<Competition> {
    Logger logger = (Logger)LoggerFactory.getLogger(CompetitionEditingFormFactory.class);
    
	CompetitionEditingFormFactory(Class<Competition> domainType) {
		super(domainType);
	}

	/** 
	 * Override this method if you need to add custom validations
	 * 
	 * @see org.vaadin.crudui.form.AbstractAutoGeneratedCrudFormFactory#bindField(com.vaadin.flow.component.HasValue, java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void bindField(HasValue field, String property, Class<?> propertyType) {
		if ("competitionDate".equals(property)) {
			LocalDateField f = ((LocalDateField)field);
			Validator<LocalDate> v = f.formatValidation(OwlcmsSession.getLocale());
			binder.forField(f).withValidator(v).bind(property);		
		} else {
			super.bindField(field, property, propertyType);
		}
	}

	@Override
	public Collection<Competition> findAll() {
		// not used
		return null;
	}

	@Override
	public Competition add(Competition domainObjectToAdd) {
		// not used
		return null;
	}

	@Override
	public Competition update(Competition ignored) {
		CompetitionRepository.save(Competition.getCurrent());
		return null;
	}

	@Override
	public void delete(Competition domainObjectToDelete) {
		// not used
	}
}