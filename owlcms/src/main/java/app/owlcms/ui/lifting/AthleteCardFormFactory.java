/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */

package app.owlcms.ui.lifting;

import java.util.Collection;

import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.github.appreciated.css.grid.GridLayoutComponent.ColumnAlign;
import com.github.appreciated.css.grid.GridLayoutComponent.RowAlign;
import com.github.appreciated.css.grid.sizes.Flex;
import com.github.appreciated.css.grid.sizes.Int;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.github.appreciated.layout.GridLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.ClassList;

import app.owlcms.components.fields.ValidationUtils;
import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.fieldofplay.FOPEvent;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.ui.crudui.OwlcmsCrudFormFactory;
import app.owlcms.ui.shared.AthleteGridContent;
import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Logger;

@SuppressWarnings("serial")
public class AthleteCardFormFactory extends OwlcmsCrudFormFactory<Athlete> {

    final private static Logger logger = (Logger) LoggerFactory.getLogger(AthleteCardFormFactory.class);

    private static final int HEADER = 1;
    private static final int AUTOMATIC = HEADER + 1;
    private static final int DECLARATION = AUTOMATIC + 1;
    private static final int CHANGE1 = DECLARATION + 1;
    private static final int CHANGE2 = CHANGE1 + 1;
    private static final int ACTUAL = CHANGE2 + 1;

    private static final int LEFT = 1;
    private static final int SNATCH1 = LEFT + 1;
    private static final int SNATCH2 = SNATCH1 + 1;
    private static final int SNATCH3 = SNATCH2 + 1;
    private static final int CJ1 = SNATCH3 + 1;
    private static final int CJ2 = CJ1 + 1;
    private static final int CJ3 = CJ2 + 1;
    private TextField cj2AutomaticProgression;
    private TextField cj3AutomaticProgression;
    private TextField cj1ActualLift;
    private TextField cj2ActualLift;
    private TextField cj3ActualLift;

    private TextField snatch2AutomaticProgression;
    private TextField snatch3AutomaticProgression;
    private TextField snatch1ActualLift;
    private TextField snatch2ActualLift;
    private TextField snatch3ActualLift;

    /**
     * text field array to facilitate setting focus when form is opened
     */
    TextField[][] textfields = new TextField[ACTUAL][CJ3];

    private Athlete editedAthlete;
    private Athlete originalAthlete;

    private AthleteGridContent origin;

    private GridLayout gridLayout;

    public AthleteCardFormFactory(Class<Athlete> domainType, AthleteGridContent origin) {
        super(domainType);
        this.origin = origin;
    }

    /**
     * @see org.vaadin.crudui.crud.CrudListener#add(java.lang.Object)
     */
    @Override
    public Athlete add(Athlete Athlete) {
        AthleteRepository.save(Athlete);
        return Athlete;
    }

    /**
     * @see org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory#buildCaption(org.vaadin.crudui.crud.CrudOperation,
     *      java.lang.Object)
     */
    @Override
    public String buildCaption(CrudOperation operation, final Athlete aFromDb) {
        // If getFullId() is null, caller will build a defaut caption, so this is safe
        return aFromDb.getFullId();
    }

    /**
     * We create a copy of the edited object so that we can validate live
     *
     * @see app.owlcms.ui.crudui.OwlcmsCrudFormFactory#buildNewForm(org.vaadin.crudui.crud.CrudOperation,
     *      java.lang.Object, boolean,
     *      com.vaadin.flow.component.ComponentEventListener,
     *      com.vaadin.flow.component.ComponentEventListener,
     *      com.vaadin.flow.component.ComponentEventListener)
     */
    @Override
    public Component buildNewForm(CrudOperation operation, Athlete aFromDb, boolean readOnly,
            ComponentEventListener<ClickEvent<Button>> cancelButtonClickListener,
            ComponentEventListener<ClickEvent<Button>> updateButtonClickListener,
            ComponentEventListener<ClickEvent<Button>> deleteButtonClickListener) {
        logger.trace("building athlete card form {}", LoggerUtils.whereFrom());
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeFull();
        if (this.responsiveSteps != null) {
            formLayout.setResponsiveSteps(this.responsiveSteps);
        }

        gridLayout = setupGrid();
        errorLabel = new Label();
        HorizontalLayout labelWrapper = new HorizontalLayout(errorLabel);
        labelWrapper.addClassName("errorMessage");
        labelWrapper.setWidthFull();
        labelWrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        // We use a copy so that if the user cancels, we still have the original object.
        // This allows us to use cleaner validation methods coded in the Athlete class
        // as opposed to
        // tedious validations on the form fields using getValue().
        editedAthlete = new Athlete();
        originalAthlete = aFromDb;
        Athlete.copy(editedAthlete, originalAthlete);
        editedAthlete.setValidation(false); // turn off validation in the setters; vaadin will call validation routines
        // explicitly

        logger.trace("aFromDb = {} {}", System.identityHashCode(aFromDb), aFromDb);
        logger.trace("originalAthlete = {} {}", System.identityHashCode(originalAthlete), originalAthlete);
        logger.trace("editedAthlete = {} {}", System.identityHashCode(editedAthlete), editedAthlete);

        bindGridFields(operation);

        Component footerLayout = this.buildFooter(operation, editedAthlete, cancelButtonClickListener,
                updateButtonClickListener, deleteButtonClickListener);

        com.vaadin.flow.component.orderedlayout.VerticalLayout mainLayout = new VerticalLayout(formLayout, gridLayout,
                errorLabel, footerLayout);
        gridLayout.setSizeFull();
        mainLayout.setFlexGrow(1, gridLayout);
        mainLayout.setHorizontalComponentAlignment(Alignment.END, footerLayout);
        mainLayout.setMargin(false);
        mainLayout.setPadding(false);
        return mainLayout;
    }

    public int computeAutomaticProgression(int value) {
        return value <= 0 ? Math.abs(value) : value + 1;
    }

    public TextField createActualWeightField(int row, int col) {
        TextField tf = new TextField();
        tf.setPattern("^[-]{0,1}\\d*$");
        tf.setPreventInvalidInput(true);
        tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
        return tf;
    }

    public TextField createPositiveWeightField(int row, int col) {
        TextField tf = new TextField();
        tf.setPattern("^\\d*$");
        tf.setPreventInvalidInput(true);
        tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
        return tf;
    }

    /**
     * Workaround for the fact that ENTER as keyboard shortcut prevents the value
     * being typed from being set in the underlying object.
     *
     * i.e. Typing TAB followed by ENTER works (because tab causes ON_BLUR), but
     * ENTER alone doesn't. We work around this issue by causing focus to move, and
     * reacting to the focus being set.
     *
     * @param operation
     *
     * @param operation
     * @param gridLayout
     *
     * @see app.owlcms.ui.crudui.OwlcmsCrudFormFactory#defineUpdateTrigger(org.vaadin.crudui.crud.CrudOperation,
     *      com.github.appreciated.layout.GridLayout)
     */
    @Override
    public TextField defineOperationTrigger(CrudOperation operation, Athlete domainObject,
            ComponentEventListener<ClickEvent<Button>> action) {
        TextField operationTrigger = new TextField();
        operationTrigger.setReadOnly(true);
        operationTrigger.setTabIndex(-1);
        operationTrigger.addFocusListener((f) -> {
            if (valid) {
                logger.debug("updating");
                doUpdate();
            } else {
                logger.debug("not updating");
            }
        });
        // field must visible and added to the layout for focus() to work, so we hide it
        // brutally
        atRowAndColumn(gridLayout, operationTrigger, AUTOMATIC, SNATCH1);
        operationTrigger.getStyle().set("z-index", "-10");
        return operationTrigger;
    }

    /**
     * @see org.vaadin.crudui.crud.CrudListener#delete(java.lang.Object)
     */
    @Override
    public void delete(Athlete notUsed) {
        AthleteRepository.delete(originalAthlete);
    }

    /**
     * @see org.vaadin.crudui.crud.CrudListener#findAll()
     */
    @Override
    public Collection<Athlete> findAll() {
        throw new UnsupportedOperationException(); // should be called on the grid
    }

    public Athlete getEditedAthlete() {
        return editedAthlete;
    }

    public Athlete getOriginalAthlete() {
        return originalAthlete;
    }

    public void setActualLiftStyle(BindingValidationStatus<?> status) throws NumberFormatException {
        TextField field = (TextField) status.getField();
        if (status.isError()) {
            field.getElement().getClassList().set("error", true);
            field.getElement().getClassList().set("good", false);
            field.getElement().getClassList().set("bad", false);
            field.focus();
        } else {
            String value = field.getValue();
            boolean empty = value == null || value.trim().isEmpty();
            if (empty) {
                field.getElement().getClassList().clear();
            } else if (value.equals("-")) {
                field.getElement().getClassList().clear();
                field.getElement().getClassList().set("bad", true);
            } else {
                int intValue = Integer.parseInt(value);
                field.getElement().getClassList().clear();
                field.getElement().getClassList().set((intValue <= 0 ? "bad" : "good"), true);
            }
        }
    }

    /**
     * @see org.vaadin.crudui.crud.CrudListener#update(java.lang.Object)
     */
    @Override
    public Athlete update(Athlete athleteFromDb) {
        doUpdate();
        return originalAthlete;
    }

    /**
     * @param operation
     * @param operation
     * @param gridLayout
     */
    protected void bindGridFields(CrudOperation operation) {
        binder = buildBinder(null, editedAthlete);

        TextField snatch1Declaration = createPositiveWeightField(DECLARATION, SNATCH1);
        binder.forField(snatch1Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch1Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch1Declaration, Athlete::setSnatch1Declaration);
        atRowAndColumn(gridLayout, snatch1Declaration, DECLARATION, SNATCH1);

        TextField snatch1Change1 = createPositiveWeightField(CHANGE1, SNATCH1);
        binder.forField(snatch1Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch1Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch1Change1, Athlete::setSnatch1Change1);
        atRowAndColumn(gridLayout, snatch1Change1, CHANGE1, SNATCH1);

        TextField snatch1Change2 = createPositiveWeightField(CHANGE2, SNATCH1);
        binder.forField(snatch1Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch1Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch1Change2, Athlete::setSnatch1Change2);
        atRowAndColumn(gridLayout, snatch1Change2, CHANGE2, SNATCH1);

        snatch1ActualLift = createActualWeightField(ACTUAL, SNATCH1);
        binder.forField(snatch1ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch1ActualLift(v)))
        .withValidator(ValidationUtils.checkUsing(v -> setAutomaticProgressions(editedAthlete)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getSnatch1ActualLift, Athlete::setSnatch1ActualLift);
        atRowAndColumn(gridLayout, snatch1ActualLift, ACTUAL, SNATCH1);

        snatch2AutomaticProgression = new TextField();
        snatch2AutomaticProgression.setReadOnly(true);
        snatch2AutomaticProgression.setTabIndex(-1);
        binder.forField(snatch2AutomaticProgression).bind(Athlete::getSnatch2AutomaticProgression,
                Athlete::setSnatch2AutomaticProgression);
        atRowAndColumn(gridLayout, snatch2AutomaticProgression, AUTOMATIC, SNATCH2);

        TextField snatch2Declaration = createPositiveWeightField(DECLARATION, SNATCH2);
        binder.forField(snatch2Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch2Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch2Declaration, Athlete::setSnatch2Declaration);
        atRowAndColumn(gridLayout, snatch2Declaration, DECLARATION, SNATCH2);

        TextField snatch2Change1 = createPositiveWeightField(CHANGE1, SNATCH2);
        binder.forField(snatch2Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch2Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch2Change1, Athlete::setSnatch2Change1);
        atRowAndColumn(gridLayout, snatch2Change1, CHANGE1, SNATCH2);

        TextField snatch2Change2 = createPositiveWeightField(CHANGE2, SNATCH2);
        binder.forField(snatch2Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch2Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch2Change2, Athlete::setSnatch2Change2);
        atRowAndColumn(gridLayout, snatch2Change2, CHANGE2, SNATCH2);

        snatch2ActualLift = createActualWeightField(ACTUAL, SNATCH2);
        binder.forField(snatch2ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch2ActualLift(v)))
        .withValidator(ValidationUtils.checkUsing(v -> setAutomaticProgressions(editedAthlete)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getSnatch2ActualLift, Athlete::setSnatch2ActualLift);
        atRowAndColumn(gridLayout, snatch2ActualLift, ACTUAL, SNATCH2);

        snatch3AutomaticProgression = new TextField();
        snatch3AutomaticProgression.setReadOnly(true);
        snatch3AutomaticProgression.setTabIndex(-1);
        binder.forField(snatch3AutomaticProgression).bind(Athlete::getSnatch3AutomaticProgression,
                Athlete::setSnatch3AutomaticProgression);
        atRowAndColumn(gridLayout, snatch3AutomaticProgression, AUTOMATIC, SNATCH3);

        TextField snatch3Declaration = createPositiveWeightField(DECLARATION, SNATCH3);
        binder.forField(snatch3Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch3Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch3Declaration, Athlete::setSnatch3Declaration);
        atRowAndColumn(gridLayout, snatch3Declaration, DECLARATION, SNATCH3);

        TextField snatch3Change1 = createPositiveWeightField(CHANGE1, SNATCH3);
        binder.forField(snatch3Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch3Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch3Change1, Athlete::setSnatch3Change1);
        atRowAndColumn(gridLayout, snatch3Change1, CHANGE1, SNATCH3);

        TextField snatch3Change2 = createPositiveWeightField(CHANGE2, SNATCH3);
        binder.forField(snatch3Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch3Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getSnatch3Change2, Athlete::setSnatch3Change2);
        atRowAndColumn(gridLayout, snatch3Change2, CHANGE2, SNATCH3);

        snatch3ActualLift = createActualWeightField(ACTUAL, SNATCH3);
        binder.forField(snatch3ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateSnatch3ActualLift(v)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getSnatch3ActualLift, Athlete::setSnatch3ActualLift);
        atRowAndColumn(gridLayout, snatch3ActualLift, ACTUAL, SNATCH3);

        TextField cj1Declaration = createPositiveWeightField(DECLARATION, CJ1);
        binder.forField(cj1Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk1Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk1Declaration, Athlete::setCleanJerk1Declaration);
        atRowAndColumn(gridLayout, cj1Declaration, DECLARATION, CJ1);

        TextField cj1Change1 = createPositiveWeightField(CHANGE1, CJ1);
        binder.forField(cj1Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk1Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk1Change1, Athlete::setCleanJerk1Change1);
        atRowAndColumn(gridLayout, cj1Change1, CHANGE1, CJ1);

        TextField cj1Change2 = createPositiveWeightField(CHANGE2, CJ1);
        binder.forField(cj1Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk1Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk1Change2, Athlete::setCleanJerk1Change2);
        atRowAndColumn(gridLayout, cj1Change2, CHANGE2, CJ1);

        cj1ActualLift = createActualWeightField(ACTUAL, CJ1);
        binder.forField(cj1ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk1ActualLift(v)))
        .withValidator(ValidationUtils.checkUsing(v -> setAutomaticProgressions(editedAthlete)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getCleanJerk1ActualLift, Athlete::setCleanJerk1ActualLift);
        atRowAndColumn(gridLayout, cj1ActualLift, ACTUAL, CJ1);

        cj2AutomaticProgression = new TextField();
        cj2AutomaticProgression.setReadOnly(true);
        cj2AutomaticProgression.setTabIndex(-1);
        binder.forField(cj2AutomaticProgression).bind(Athlete::getCleanJerk2AutomaticProgression,
                Athlete::setCleanJerk2AutomaticProgression);
        atRowAndColumn(gridLayout, cj2AutomaticProgression, AUTOMATIC, CJ2);

        TextField cj2Declaration = createPositiveWeightField(DECLARATION, CJ2);
        binder.forField(cj2Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk2Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk2Declaration, Athlete::setCleanJerk2Declaration);
        atRowAndColumn(gridLayout, cj2Declaration, DECLARATION, CJ2);

        TextField cj2Change1 = createPositiveWeightField(CHANGE1, CJ2);
        binder.forField(cj2Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk2Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk2Change1, Athlete::setCleanJerk2Change1);
        atRowAndColumn(gridLayout, cj2Change1, CHANGE1, CJ2);

        TextField cj2Change2 = createPositiveWeightField(CHANGE2, CJ2);
        binder.forField(cj2Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk2Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk2Change2, Athlete::setCleanJerk2Change2);
        atRowAndColumn(gridLayout, cj2Change2, CHANGE2, CJ2);

        cj2ActualLift = createActualWeightField(ACTUAL, CJ2);
        binder.forField(cj2ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk2ActualLift(v)))
        .withValidator(ValidationUtils.checkUsing(v -> setAutomaticProgressions(editedAthlete)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getCleanJerk2ActualLift, Athlete::setCleanJerk2ActualLift);
        atRowAndColumn(gridLayout, cj2ActualLift, ACTUAL, CJ2);

        cj3AutomaticProgression = new TextField();
        cj3AutomaticProgression.setReadOnly(true);
        cj3AutomaticProgression.setTabIndex(-1);
        binder.forField(cj3AutomaticProgression).bind(Athlete::getCleanJerk3AutomaticProgression,
                Athlete::setCleanJerk3AutomaticProgression);
        atRowAndColumn(gridLayout, cj3AutomaticProgression, AUTOMATIC, CJ3);

        TextField cj3Declaration = createPositiveWeightField(DECLARATION, CJ3);
        binder.forField(cj3Declaration)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk3Declaration(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk3Declaration, Athlete::setCleanJerk3Declaration);
        atRowAndColumn(gridLayout, cj3Declaration, DECLARATION, CJ3);

        TextField cj3Change1 = createPositiveWeightField(CHANGE1, CJ3);
        binder.forField(cj3Change1)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk3Change1(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk3Change1, Athlete::setCleanJerk3Change1);
        atRowAndColumn(gridLayout, cj3Change1, CHANGE1, CJ3);

        TextField cj3Change2 = createPositiveWeightField(CHANGE2, CJ3);
        binder.forField(cj3Change2)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk3Change2(v)))
        .withValidationStatusHandler(status -> {
        }).bind(Athlete::getCleanJerk3Change2, Athlete::setCleanJerk3Change2);
        atRowAndColumn(gridLayout, cj3Change2, CHANGE2, CJ3);

        cj3ActualLift = createActualWeightField(ACTUAL, CJ3);
        binder.forField(cj3ActualLift)
        .withValidator(ValidationUtils.checkUsing(v -> editedAthlete.validateCleanJerk3ActualLift(v)))
        .withValidationStatusHandler(status -> setActualLiftStyle(status))
        .bind(Athlete::getCleanJerk3ActualLift, Athlete::setCleanJerk3ActualLift);
        atRowAndColumn(gridLayout, cj3ActualLift, ACTUAL, CJ3);

        // use setBean so that changes are immediately reflected to the working copy
        // otherwise the changes are only visible in the fields, and the validation
        // routines in the
        // Athlete class don't work
        binder.setBean(editedAthlete);
        setFocus(editedAthlete);
    }

    /**
     * Add bean-level validations
     *
     * @see org.vaadin.crudui.form.AbstractAutoGeneratedCrudFormFactory#buildBinder(org.vaadin.crudui.crud.CrudOperation,
     *      java.lang.Object)
     */
    @Override
    protected Binder<Athlete> buildBinder(CrudOperation operation, Athlete doNotUse) {
        // we do *not* use the athlete provided by the grid selection. For some reason,
        // the grid selector returns a copy on the first invocation, instead of the
        // underlying object.
        // we use editedAthlete, which this form retrieves from the underlying data
        // source
        binder = super.buildBinder(operation, editedAthlete);
        logger.trace("athlete from grid={} edited={}", doNotUse, editedAthlete);
        setValidationStatusHandler(true);
        return binder;

    }

    /**
     * @see app.owlcms.ui.crudui.OwlcmsCrudFormFactory#buildFooter(org.vaadin.crudui.crud.CrudOperation,
     *      java.lang.Object, com.vaadin.flow.component.ComponentEventListener,
     *      com.vaadin.flow.component.ComponentEventListener,
     *      com.vaadin.flow.component.ComponentEventListener)
     */
    @Override
    protected Component buildFooter(CrudOperation operation, Athlete unused,
            ComponentEventListener<ClickEvent<Button>> cancelButtonClickListener,
            ComponentEventListener<ClickEvent<Button>> unused2, ComponentEventListener<ClickEvent<Button>> unused3) {
        ComponentEventListener<ClickEvent<Button>> postOperationCallBack = (e) -> {};
        Button operationButton = null;
        if (operation == CrudOperation.UPDATE) {
            operationButton = buildOperationButton(CrudOperation.UPDATE, originalAthlete, postOperationCallBack);
        } else if (operation == CrudOperation.ADD) {
            operationButton = buildOperationButton(CrudOperation.ADD, originalAthlete, postOperationCallBack);
        }
        Button deleteButton = buildDeleteButton(CrudOperation.DELETE, originalAthlete, null);
        Button withdrawButton = buildWithdrawButton();
        Checkbox forcedCurrentCheckbox = buildForcedCurrentCheckbox();
        Button cancelButton = buildCancelButton(cancelButtonClickListener);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidth("100%");
        footerLayout.setSpacing(true);
        footerLayout.setPadding(false);

        if (deleteButton != null && operation != CrudOperation.ADD) {
            footerLayout.add(deleteButton);
        }
        if (withdrawButton != null && operation != CrudOperation.ADD) {
            footerLayout.add(withdrawButton);
        }
        if (forcedCurrentCheckbox != null && operation != CrudOperation.ADD) {
            footerLayout.add(forcedCurrentCheckbox);
        }

        Label spacer = new Label();

        footerLayout.add(spacer, operationTrigger);

        if (cancelButton != null) {
            footerLayout.add(cancelButton);
        }

        if (operationButton != null) {
            footerLayout.add(operationButton);
            if (operation == CrudOperation.UPDATE) {
                ShortcutRegistration reg = operationButton.addClickShortcut(Key.ENTER);
                reg.allowBrowserDefault();
            }
        }
        footerLayout.setFlexGrow(1.0, spacer);
        return footerLayout;
    }

    /**
     * Special version because we use setBean instead of readBean
     *
     * @see app.owlcms.ui.crudui.OwlcmsCrudFormFactory#buildOperationButton(org.vaadin.crudui.crud.CrudOperation,
     *      java.lang.Object, com.vaadin.flow.component.ComponentEventListener)
     */
    @Override
    protected Button buildOperationButton(CrudOperation operation, Athlete domainObject,
            ComponentEventListener<ClickEvent<Button>> callBack) {
        if (callBack == null)
            return null;
        Button button = doBuildButton(operation);
        operationTrigger = defineOperationTrigger(operation, domainObject, callBack);

        ComponentEventListener<ClickEvent<Button>> listener = event -> {
            // force value to be written to underlying bean. Crude Workaround for keyboard shortcut
            // which does not process last field input when ENTER key is used.
            operationTrigger.focus();
        };

        button.addClickListener(listener);
        return button;
    }

    /**
     * Force correcting one error at a time
     *
     * @param validationStatus
     * @return
     */
    @Override
    protected boolean setErrorLabel(BinderValidationStatus<?> validationStatus, boolean updateFieldStatus) {
        String simpleName = this.getClass().getSimpleName();
        logger.debug("{} validations", simpleName);
        StringBuilder sb = new StringBuilder();

        boolean hasErrors = validationStatus.getFieldValidationErrors().size() > 0;
        validationStatus.getBinder().getFields().forEach(f -> {
            ClassList fieldClasses = ((Component) f).getElement().getClassList();
            fieldClasses.set("error", false);
            f.setReadOnly(hasErrors);
        });
        TextField field = null;
        for (BindingValidationStatus<?> ve : validationStatus.getFieldValidationErrors()) {
            field = (TextField) ve.getField();
            ClassList fieldClasses = field.getElement().getClassList();
            fieldClasses.clear();
            fieldClasses.set("error", true);
            field.setReadOnly(false);
            field.setAutoselect(true);
            field.focus();
            if (sb.length() > 0)
            {
                sb.append("; ");
            }
            String message = ve.getMessage().orElse(field.getTranslation("Error"));
            sb.append(message);
        }
        for (ValidationResult ve : validationStatus.getBeanValidationErrors()) {
            if (sb.length() > 0)
            {
                sb.append("; ");
            }
            String message = ve.getErrorMessage();
            // logger.debug("bean message: {}",message);
            sb.append(message);
        }
        if (sb.length() > 0) {
            String message = sb.toString();
            logger.debug("{} setting message {}", simpleName, message);
            errorLabel.setVisible(true);
            errorLabel.getElement().setProperty("innerHTML", message);
            errorLabel.getClassNames().set("errorMessage", true);
        } else {
            logger.debug("{} setting EMPTY", simpleName);
            errorLabel.setVisible(true);
            errorLabel.getElement().setProperty("innerHTML", "&nbsp;");
            errorLabel.getClassNames().clear();
        }
        if (!hasErrors) {
            resetReadOnlyFields();
        } else if (field != null) {
            field.focus();
        }
        return hasErrors;
    }

    protected GridLayout setupGrid() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setTemplateRows(new Repeat(ACTUAL, new Flex(1)));
        gridLayout.setTemplateColumns(new Repeat(CJ3, new Flex(1)));
        gridLayout.setGap(new Length("0.8ex"), new Length("1.2ex"));

        // column headers
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Snatch1")), HEADER, SNATCH1, RowAlign.CENTER, ColumnAlign.CENTER);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Snatch2")), HEADER, SNATCH2, RowAlign.CENTER, ColumnAlign.CENTER);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Snatch3")), HEADER, SNATCH3, RowAlign.CENTER, ColumnAlign.CENTER);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("C_and_J_1")), HEADER, CJ1, RowAlign.CENTER, ColumnAlign.CENTER);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("C_and_J_2")), HEADER, CJ2, RowAlign.CENTER, ColumnAlign.CENTER);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("C_and_J_3")), HEADER, CJ3, RowAlign.CENTER, ColumnAlign.CENTER);

        // row headings
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("AutomaticProgression")), AUTOMATIC, LEFT, RowAlign.CENTER,
                ColumnAlign.END);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Declaration")), DECLARATION, LEFT, RowAlign.CENTER, ColumnAlign.END);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Change_1")), CHANGE1, LEFT, RowAlign.CENTER, ColumnAlign.END);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("Change_2")), CHANGE2, LEFT, RowAlign.CENTER, ColumnAlign.END);
        atRowAndColumn(gridLayout, new Label(gridLayout.getTranslation("WeightLifted")), ACTUAL, LEFT, RowAlign.CENTER, ColumnAlign.END);

        return gridLayout;
    }

    private void atRowAndColumn(GridLayout gridLayout, Component component, int row, int column) {
        atRowAndColumn(gridLayout, component, row, column, RowAlign.CENTER, ColumnAlign.CENTER);
    }

    private void atRowAndColumn(GridLayout gridLayout, Component component, int row, int column, RowAlign ra,
            ColumnAlign ca) {
        gridLayout.add(component);
        gridLayout.setRowAndColumn(component, new Int(row), new Int(column), new Int(row), new Int(column));
        gridLayout.setRowAlign(component, ra);
        gridLayout.setColumnAlign(component, ca);
        component.getElement().getStyle().set("width", "6em");
        if (component instanceof TextField) {
            TextField textField = (TextField) component;
            textfields[row - 1][column - 1] = textField;
        }

    }

    private Button buildWithdrawButton() {
        Button withdrawalButton = new Button(Translator.translate("Withdrawal"),IronIcons.EXIT_TO_APP.create(),(e) -> {
            Athlete.copy(originalAthlete, editedAthlete);
            originalAthlete.withdraw();
            AthleteRepository.save(originalAthlete);
            OwlcmsSession.withFop((fop) -> {
                fop.getFopEventBus().post(new FOPEvent.WeightChange(this.getOrigin(), originalAthlete));
            });
            origin.closeDialog();
        });
        withdrawalButton.getElement().setAttribute("theme", "error");
        return withdrawalButton;
    }
    
    private Checkbox buildForcedCurrentCheckbox() {
        Checkbox checkbox = new Checkbox(Translator.translate("ForcedAsCurrent"));
        checkbox.getElement().getStyle().set("margin-left", "3em");
        binder.forField(checkbox).bind(Athlete::isForcedAsCurrent, Athlete::setForcedAsCurrent);
        return checkbox;
    }

    /**
     * Update the original athlete so that the lifting order picks up the change.
     */
    private void doUpdate() {
        Athlete.copy(originalAthlete, editedAthlete);
        AthleteRepository.save(originalAthlete);
        OwlcmsSession.withFop((fop) -> {
            fop.getFopEventBus().post(new FOPEvent.WeightChange(this.getOrigin(), originalAthlete));
        });
        origin.closeDialog();
    }

    private Object getOrigin() {
        return origin;
    }

    private void resetReadOnlyFields() {
        snatch2AutomaticProgression.setReadOnly(true);
        snatch3AutomaticProgression.setReadOnly(true);
        cj2AutomaticProgression.setReadOnly(true);
        cj3AutomaticProgression.setReadOnly(true);
    }

    /**
     * set the automatic progressions. This is invoked as a validator because we
     * don't want to be called if the entered value is invalid. Only the side-effect
     * is interesting, so we return true.
     *
     * @param athlete
     * @return true always
     */
    private boolean setAutomaticProgressions(Athlete athlete) {
        int value = Athlete.zeroIfInvalid(snatch1ActualLift.getValue());
        int autoVal = computeAutomaticProgression(value);
        snatch2AutomaticProgression.setValue(Integer.toString(autoVal));
        value = Athlete.zeroIfInvalid(snatch2ActualLift.getValue());
        autoVal = computeAutomaticProgression(value);
        snatch3AutomaticProgression.setValue(Integer.toString(autoVal));

        value = Athlete.zeroIfInvalid(cj1ActualLift.getValue());
        autoVal = computeAutomaticProgression(value);
        cj2AutomaticProgression.setValue(Integer.toString(autoVal));
        value = Athlete.zeroIfInvalid(cj2ActualLift.getValue());
        autoVal = computeAutomaticProgression(value);
        cj3AutomaticProgression.setValue(Integer.toString(autoVal));

        return true;
    }

    private void setFocus(Athlete a) {
        int targetRow = ACTUAL + 1;
        int targetCol = CJ3 + 1;

        // figure out whether we are searching for snatch or CJ
        int rightCol;
        int leftCol;
        if (a.getAttemptsDone() >= 3) {
            rightCol = CJ3;
            leftCol = CJ1;
        } else {
            rightCol = SNATCH3;
            leftCol = SNATCH1;
        }

        // remember location of last empty cell, going backwards
        search: for (int col = rightCol; col >= leftCol; col--) {
            for (int row = ACTUAL; row > AUTOMATIC; row--) {
                boolean empty = textfields[row - 1][col - 1].isEmpty();
                if (empty) {
                    targetRow = row - 1;
                    targetCol = col - 1;
                } else {
                    // don't go back past first non-empty (leave holes)
                    break search;
                }
            }
        }

        if (targetCol <= CJ3 && targetRow <= ACTUAL) {
            // a suitable empty cell was found, set focus
            textfields[targetRow][targetCol].setAutofocus(true);
            textfields[targetRow][targetCol].setAutoselect(true);
        }
    }
}
