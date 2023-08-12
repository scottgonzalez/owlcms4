import { html, LitElement, css } from "lit";
/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/

class Monitor extends LitElement {
  static get is() {
    return "eventmonitor-template";
  }

  render() {
    return html`
      <link
        rel="stylesheet"
        type="text/css"
        .href="${"local/" +
        (this.stylesDir ?? "") +
        "/" +
        (this.video ?? "") +
        "colors" +
        (this.autoversion ?? "")}"
      />
      <link
        rel="stylesheet"
        type="text/css"
        .href="${"local/" +
        (this.stylesDir ?? "") +
        "/" +
        (this.video ?? "") +
        "eventmonitor" +
        (this.autoversion ?? "")}"
      />
      <div class="wrapper">
        <div class="${"notification " + (this.notificationClass ?? "")}">
          ${this.title}
        </div>
      </div>
    `;
  }

  firstUpdated(_changedProperties) {
    //console.debug("monitor is ready");
    super.firstUpdated(_changedProperties);
  }

  setTitle(title) {
    //console.log("title = "+title);
    document.title = title;
  }
}

customElements.define(Monitor.is, Monitor);
