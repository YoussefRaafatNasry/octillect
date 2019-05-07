package octillect.controllers.dialogs;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.validation.RequiredFieldValidator;

import java.util.Calendar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import octillect.controllers.ApplicationController;
import octillect.controllers.Injectable;
import octillect.controls.OButton;
import octillect.database.repositories.ColumnRepository;
import octillect.database.repositories.TaskRepository;
import octillect.database.firebase.FirestoreAPI;
import octillect.models.Column;
import octillect.models.Task;
import octillect.models.builders.TaskBuilder;

public class NewTaskDialogController implements Injectable<ApplicationController> {

    // FXML Fields
    @FXML public JFXDialog newTaskDialog;
    @FXML public JFXTextField newTaskNameTextField;
    @FXML public JFXTextArea newTaskDescriptionTextArea;
    @FXML public OButton addTaskButton;

    public Column currentColumn;

    // Injected Controllers
    private ApplicationController applicationController;

    // Empty field validation
    RequiredFieldValidator requiredFieldValidator;

    @Override
    public void init() {
        requiredFieldValidator = new RequiredFieldValidator("Required field.");
        newTaskNameTextField.getValidators().add(requiredFieldValidator);
        newTaskNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                newTaskNameTextField.validate();
            }
        });
    }

    @Override
    public void inject(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public void handleAddTaskButtonAction(ActionEvent actionEvent) {
        newTaskNameTextField.validate();
        if(!requiredFieldValidator.getHasErrors()) {

            Task newTask = new TaskBuilder().with($ -> {
                $.id = FirestoreAPI.getInstance().encryptWithDateTime(newTaskNameTextField.getText() + applicationController.user.getId());
                $.name = newTaskNameTextField.getText();
                $.description = newTaskDescriptionTextArea.getText();
                $.creationDate = Calendar.getInstance().getTime();
                $.creator = applicationController.user;
            }).build();

            currentColumn.getTasks().add(newTask);

            ColumnRepository.getInstance().addTask(currentColumn.getId(), newTask.getId());
            TaskRepository.getInstance().add(newTask);

            newTaskDialog.close();
        }
    }

    public void handleNewTaskDialogClosed(JFXDialogEvent jfxDialogEvent) {
        newTaskNameTextField.resetValidation();
        newTaskNameTextField.setText("");
        newTaskDescriptionTextArea.setText("");
    }
}