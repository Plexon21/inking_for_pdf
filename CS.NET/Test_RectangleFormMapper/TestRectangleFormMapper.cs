using System;
using System.Collections.Generic;
using System.Windows;
using RectangleFormMapper;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace Test_RectangleFormMapper
{
    [TestClass]
    public class TestRectangleFormMapper
    {
        [TestMethod]
        public void TestMapToFormDoubleCorrect()
        {
            double[] input = { 1.1, 1.2, 2.1, 2.2, 3.1, 3.2, 4.1, 4.2 };

            var expected = new List<double[]> {
                new double[]{1.1, 1.2, 1.1, 4.2},
                new double[]{1.1, 1.2, 4.1, 1.2},
                new double[]{1.1, 4.2, 4.1, 4.2},
                new double[]{4.1, 1.2, 4.1, 4.2}
            };
            var rect = new RectangleFormMapper.RectangleFormMapper();
            var output = rect.MapToForm(input);

            Assert.AreEqual(expected.Count, output.Count);
            for (int k = 0; k < expected.Count; k++)
            {
                for (int i = 0; i < expected[k].Length; i++)
                {
                    Assert.AreEqual(expected[k][i], output[k][i]);
                }
            }
        }

        [TestMethod]
        public void TestMapToFormPointsCorrect()
        {
            var input = new List<Point>
            {
                new Point(1.1, 1.2),
                new Point(2.1, 2.2),
                new Point(3.1, 3.2),
                new Point(4.1, 4.2)
            };
            var expected = new List<Point>
            {
                new Point(1.1, 1.2),
                new Point(1.1, 4.2),
                new Point(4.1, 4.2),
                new Point(4.1, 1.2),
                new Point(1.1, 1.2)
            };
            var rect = new RectangleFormMapper.RectangleFormMapper();
            var output = rect.MapToForm(input);
            
            Assert.AreEqual(expected.Count, output.Count);
            for (int k = 0; k < expected.Count; k++)
            {
                Assert.AreEqual(expected[k].X, output[k].X);
                Assert.AreEqual(expected[k].Y, output[k].Y);
            }
        }

        [TestMethod]
        public void TestMapToFormDoubleFails()
        {
            double[] input = null;
            var rect = new RectangleFormMapper.RectangleFormMapper();
            var output = rect.MapToForm(input);
            Assert.IsNull(output);

            input = new double[0];
            output = rect.MapToForm(input);
            Assert.IsNull(output);

            input = new [] {1.1, 1.2, 1.3};
            output = rect.MapToForm(input);
            Assert.IsNull(output);
        }

        [TestMethod]
        public void TestMapToFormPointFails()
        {
            List<Point> input = null;
            var rect = new RectangleFormMapper.RectangleFormMapper();
            var output = rect.MapToForm(input);
            Assert.IsNull(output);

            input = new List<Point>();
            output = rect.MapToForm(input);
            Assert.IsNull(output);

            input = new List<Point>
            {
                new Point(1.1, 1.2)
            };
            output = rect.MapToForm(input);
            Assert.IsNull(output);
        }

    }
}
