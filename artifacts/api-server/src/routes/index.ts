import { Router, type IRouter } from "express";
import healthRouter from "./health";
import rescuexRouter from "./rescuex";

const router: IRouter = Router();

router.use(healthRouter);
router.use(rescuexRouter);

export default router;
