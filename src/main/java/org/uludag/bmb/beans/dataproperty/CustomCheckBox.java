package org.uludag.bmb.beans.dataproperty;

import javafx.scene.control.CheckBox;

public class CustomCheckBox extends CheckBox {

    public CustomCheckBox(int status) {
        this.disableProperty().set(true);
        if (status == 1) {
            this.selectedProperty().set(true);
        } else {
            this.selectedProperty().set(false);
        }
    }
}
