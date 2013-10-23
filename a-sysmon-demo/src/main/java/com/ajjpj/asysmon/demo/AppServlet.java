package com.ajjpj.asysmon.demo;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.AWithParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * @author arno
 */
public class AppServlet extends HttpServlet {
    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter out = resp.getWriter();
        out.println ("<html><head><title>A-SysMon demo content</title></head><body><h1>A-SysMon demo content</h1></body></html>");

        sleep();

        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                ASysMon.get().measure("x", new AMeasureCallback<Object, RuntimeException>() {
                    @Override
                    public Object call(AWithParameters m) {
                        return sleep();
                    }
                });
                return sleep();
            }
        });
        ASysMon.get().measure("c", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
    }

    private Object sleep() {
        try {
            Thread.sleep(20 + new Random().nextInt(20));
        } catch (InterruptedException exc) {
            throw new RuntimeException(exc);
        }
        return null;
    }
}
