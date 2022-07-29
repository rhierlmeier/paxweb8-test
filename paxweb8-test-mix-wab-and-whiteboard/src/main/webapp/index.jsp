<%@page contentType="text/html; charset=UTF-8"
        pageEncoding="ISO-8859-1" session="false" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>WAB and Whiteboard Servlet share same session</title>
    <script>
        function init() {
            document.getElementById("buttonGetWab").addEventListener('click', getViaWab);
            document.getElementById("buttonGetWhiteboard").addEventListener('click', getViaWhiteboard);
            document.getElementById("buttonIncrementWab").addEventListener('click', incrementViaWab);
            document.getElementById("buttonIncrementWhiteboard").addEventListener('click', incrementViaWhiteboard);

        }

        let wabUri = "./counter";
        let whiteboardUri = "./whiteboard/counter";

        function getViaWab() {
            request(wabUri, "GET", "counterWab");
        }

        function getViaWhiteboard() {
            request(whiteboardUri, "GET", "counterWhiteboard");
        }

        function incrementViaWhiteboard() {
            request(whiteboardUri, "POST", "counterWhiteboard");
        }

        function incrementViaWab() {
            request(wabUri, "POST", "counterWab");
        }

        function request(counterUri, httpMethodName, resultId) {
            let httpRequest = new XMLHttpRequest();

            if (!httpRequest) {
                alert('Giving up :( Cannot create an XMLHTTP instance');
                return false;
            }
            httpRequest.onreadystatechange = function () {
                if (httpRequest.readyState === XMLHttpRequest.DONE) {
                    let element = document.getElementById(resultId)
                    if (httpRequest.status === 200) {
                        let json = JSON.parse(httpRequest.responseText);
                        element.innerText = json.count;
                    } else {
                        element.innerText = "Failure: " + httpRequest.status + ": " + httpRequest.statusText;
                    }
                }
            };
            httpRequest.open(httpMethodName, counterUri);
            httpRequest.send();
        }

    </script>

</head>
<body onload="init();">

<h1>WAB and Whiteboard Servlet share same session</h1>

<p>
    Demonstrates that a servlet that is deployed via OSGI Http Whiteboard get the same HTTP session a
    as a servlet that is part of a Web Application bundle.<br>
    The web application has two Servlets deployed.<br>
</p>
<ul>
    <li>One with OSGI whiteboard under /wab/whiteboard/counter</li>
    <li>One as WebServlet as part of the Web Application Bundle under /wab/counter</li>
</ul>
<p>
    Both Servlets increments with an HTTP Post the session attribute counter by one.
    With an HTTP Get the read this session attribute.

</p>

<table style="border-color: black; border-width: 2px;">
    <tr>
        <th>Counter from WAB session:</th>
        <td>
            <div id="counterWab">Yet not clicked</div>
        </td>
        <td>
            <button id="buttonGetWab">GET /wab/counter</button>
        </td>
        <td>
            <button id="buttonIncrementWab">POST /wab/counter</button>
        </td>
    </tr>
    <tr>
        <th>Counter from Whiteboard session:</th>
        <td>
            <div id="counterWhiteboard">Yet not clicked</div>
        </td>
        <td>
            <button id="buttonGetWhiteboard">GET /wab/whiteboard/counter</button>
        </td>
        <td>
            <button id="buttonIncrementWhiteboard">POST /wab/whiteboard/counter</button>
        </td>
    </tr>
</table>


</body>
</html>