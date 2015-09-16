// imports
var Router = Java.type('io.u.yoke.handler.Router');

// main
var router = new Router();

router.get("/user/:id", function (ctx) {
  ctx.next()
});

router.get("/user/:id", function (ctx) {
  ctx.putAt("name", ctx.request().getParam("id"));
  ctx.response().render("template.hbs");
});

router.param("id", /gr.*/);

yoke.use(router);

yoke.use(function (ctx) {
  ctx.next();
});

yoke.listen(8080);
print('Ready!');