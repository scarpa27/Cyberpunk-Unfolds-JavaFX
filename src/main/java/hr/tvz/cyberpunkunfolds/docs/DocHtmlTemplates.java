package hr.tvz.cyberpunkunfolds.docs;

final class DocHtmlTemplates {
    static final String SIDEBAR_ITEM = """
        <a href="#%s">%s</a>
        <div class="sub">%s</div>
        """;

    static final String BADGE = "<span class=\"badge %s\">%s</span>";

    static final String MEMBER = """
        <div class="member" data-kind="%s" data-visibility="%s" data-synth="%s" data-search="%s">
          <div class="hdr">
            <div class="memberName">%s</div>
            <div class="tags">%s</div>
          </div>
          <pre class="code">%s</pre>
        </div>
        """;

    static final String TOOLBAR = """
            <div class="toolbar">
              <input id="q" type="text" placeholder="Search classes / members… (e.g. Lobby, save, RemoteException)">
            
              <span class="chip"><input id="secFields" type="checkbox" checked> Fields</span>
              <span class="chip"><input id="secConstructors" type="checkbox" checked> Constructors</span>
              <span class="chip"><input id="secMethods" type="checkbox" checked> Methods</span>
            
              <span class="sep"></span>
            
              <span class="chip"><input id="visPublic" type="checkbox" checked> public</span>
              <span class="chip"><input id="visProtected" type="checkbox" checked> protected</span>
              <span class="chip"><input id="visPackage" type="checkbox" checked> package</span>
              <span class="chip"><input id="visPrivate" type="checkbox"> private</span>
            
              <span class="sep"></span>
            
              <span class="chip"><input id="showSynth" type="checkbox"> compiler-generated</span>
            </div>
            """;

    private DocHtmlTemplates() { }
}
