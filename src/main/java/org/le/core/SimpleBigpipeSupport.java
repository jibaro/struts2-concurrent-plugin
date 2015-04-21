package org.le.core;

import org.apache.commons.lang3.StringUtils;

public class SimpleBigpipeSupport implements BigpipeSupportStrategy {

    private static SimpleBigpipeSupport instance = new SimpleBigpipeSupport();

    private SimpleBigpipeSupport() {
    }

    public static SimpleBigpipeSupport newInstance() {
        return instance;
    }

    private static final String bigpipeScript = "<script type=\"application/javascript\">\n" +
            "        function replace(id, content) {\n" +
            "            var pagelet = document.getElementById(id);\n" +
            "            pagelet.innerHTML = content;\n" +
            "        }\n" +
            "    </script>";

    @Override
    public String execute(String html) {
        StringBuilder sb = new StringBuilder();
        int indexOfHeadEnd = StringUtils.indexOfIgnoreCase(html, "</head>");
        if (indexOfHeadEnd > 0) {//在head中插入
            sb.append(html.substring(0, indexOfHeadEnd))
                    .append(bigpipeScript)
                    .append("\n")
                    .append(html.substring(indexOfHeadEnd, html.length()));
        }
        return sb.toString();
    }
}
